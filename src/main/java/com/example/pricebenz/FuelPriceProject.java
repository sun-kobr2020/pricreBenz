package com.example.pricebenz;

import org.json.JSONObject;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class FuelPriceProject {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            FuelApiClient api = new FuelApiClient();
            FuelPriceService service = new FuelPriceService();

            api.warmup();

            // 1. Получаем данные (напр. для Аи-95, ID=11)
            int targetId = 17;
            FuelData data = service.fetchAllData(api, targetId);

            // 2. Выводим информацию о заправке
            printAzsInfo(data);

            // 3. Выводим таблицу всех цен с пометками
            printPriceTable(service, data);

            // 4. Расчет расхода с указанием типа цены
            processTripReport(service, data, targetId);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void printAzsInfo(FuelData data) {
        if (data.getAzsList() != null && data.getAzsList().length() > 0) {
            JSONObject azs = data.getAzsList().getJSONObject(0);
            JSONObject brand = azs.optJSONObject("brand");
            String brandName = (brand != null) ? brand.optString("name", "Частная АЗС") : "Неизвестная сеть";

            System.out.println("\n=== ИНФОРМАЦИЯ ОБ АЗС ===");
            System.out.println("Сеть:    " + brandName);
            System.out.println("Адрес:   " + azs.optString("address", "Адрес не указан"));
        }
    }

    private static void printPriceTable(FuelPriceService service, FuelData data) {
        System.out.println("\n--- СВОДКА ЦЕН ПО РЕГИОНУ ---");
        for (var entry : FuelConfig.FUEL_MAP.entrySet()) {
            // Сначала ищем точную
            double price = 0;
            String type = "";

            if (data.getAzsList() != null && data.getAzsList().length() > 0) {
                price = service.findExactPrice(data.getAzsList().getJSONObject(0), entry.getKey(), entry.getValue());
                if (price > 0) type = "[Точная]";
            }

            if (price <= 0) {
                price = service.getAveragePrice(data.getAvgPricesMap(), entry.getKey());
                if (price > 0) type = "[Средняя]";
            }

            if (price > 0) {
                System.out.printf("%-10s: %6.2f руб. %s%n", entry.getValue(), price, type);
            } else {
                System.out.printf("%-10s: нет данных%n", entry.getValue());
            }
        }
    }

    private static void processTripReport(FuelPriceService service, FuelData data, int fuelId) {
        TripCalculator calc = new TripCalculator();
        String fuelName = FuelConfig.FUEL_MAP.getOrDefault(fuelId, "Топливо");

        // Определяем цену и её тип для отчета
        double price = 0;
        String type = "";

        if (data.getAzsList() != null && data.getAzsList().length() > 0) {
            price = service.findExactPrice(data.getAzsList().getJSONObject(0), fuelId, fuelName);
            if (price > 0) type = "[Точная]";
        }

        if (price <= 0) {
            price = service.getAveragePrice(data.getAvgPricesMap(), fuelId);
            if (price > 0) type = "[Средняя]";
        }

        if (price > 0) {
            double consumption = calc.calculateConsumption(40, 50550, 50000);
            double totalCost = calc.calculateTotalCost(40, price);

            System.out.println("\n=== ОТЧЕТ ПО ЗАПРАВКЕ ===");
            System.out.printf("Выбранное топливо:  %s%n", fuelName);
            System.out.printf("Использована цена:  %.2f руб. %s%n", price, type);
            System.out.printf("Средний расход:     %.2f л/100 км%n", consumption);
            System.out.printf("Итого к оплате:     %.2f руб.%n", totalCost);
            System.out.println("=========================\n");
        }
    }
}