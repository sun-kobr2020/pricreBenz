package com.example.pricebenz;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FuelPriceProject {
    public static void main(String[] args) throws Exception {
        FuelApiClient api = new FuelApiClient();
        FuelPriceService service = new FuelPriceService();
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        api.warmup(); // Инициализация сессии

        // Готовим запросы
        String listPayload = String.format(Locale.US, "{\"limit\":3,\"fuelId\":11,\"lat\":%.6f,\"lng\":%.6f}",
                FuelConfig.LAT, FuelConfig.LNG);
        String avgPayload = String.format(Locale.US, "{\"lat\":%.6f,\"lng\":%.6f}",
                FuelConfig.LAT, FuelConfig.LNG);

        // Получаем данные
        JSONObject listJson = new JSONObject(api.sendPost("/api/9/near/list", listPayload));
        JSONObject avgJson = new JSONObject(api.sendPost("/api/9/avgprices", avgPayload));

        // Извлекаем базовые объекты
        JSONObject dataObj = listJson.optJSONObject("data");
        JSONArray azsList = (dataObj != null) ? dataObj.optJSONArray("list") : null;
        JSONObject avgPricesMap = avgJson.optJSONObject("data") != null ?
                avgJson.getJSONObject("data").optJSONObject("avgprice") : null;

        // Вывод информации об АЗС
        if (azsList != null && azsList.length() > 0) {
            JSONObject azs = azsList.getJSONObject(0);
            System.out.println("\n=== БЛИЖАЙШАЯ АЗС ===");
            System.out.println("Сеть:    " + (azs.optJSONObject("brand") != null ? azs.getJSONObject("brand").getString("name") : "Частная"));
            System.out.println("Адрес:   " + azs.optString("address"));
        } else {
            System.out.println("\n[!] АЗС рядом не найдены.");
        }

        // Вывод таблицы цен
        System.out.println("-------------------------");
        for (var entry : FuelConfig.FUEL_MAP.entrySet()) {
            double price = 0;
            String type = "";

            // 1. Пробуем точную
            if (azsList != null && azsList.length() > 0) {
                price = service.findExactPrice(azsList.getJSONObject(0), entry.getKey(), entry.getValue());
                if (price > 0) type = "[Точная]";
            }

            // 2. Если нет точной, берем среднюю
            if (price == 0) {
                price = service.getAveragePrice(avgPricesMap, entry.getKey());
                if (price > 0) type = "[Средняя]";
            }

            if (price > 0) {
                System.out.printf("%-10s: %6.2f руб. %s%n", entry.getValue(), price, type);
            } else {
                System.out.printf("%-10s: Нет данных%n", entry.getValue());
            }
        }
        // --- ИМИТАЦИЯ ЗАПРАВКИ ---
        int prevOdo = 50000;    // Пробег при прошлой заправке
        int currentOdo = 50550; // Текущий пробег (проехали 550 км)
        double fuelAdded = 40.0; // Сколько литров залили сейчас (до полного)
        int targetFuelId = 11;  // Мы заправляемся Аи-95

        // 1. Находим актуальную цену для расчетов
        double currentPrice = 0;
        // Ищем в нашем списке цен значение для Аи-95
        // (Для примера возьмем из сервиса или напрямую из найденной АЗС)
        if (azsList != null && azsList.length() > 0) {
            currentPrice = service.findExactPrice(azsList.getJSONObject(0), targetFuelId, "Аи-95");
        }
        if (currentPrice == 0) {
            currentPrice = service.getAveragePrice(avgPricesMap, targetFuelId);
        }

        // 2. Выполняем расчеты
        TripCalculator calculator = new TripCalculator();

        if (currentPrice > 0) {
            double totalCost = calculator.calculateTotalCost(fuelAdded, currentPrice);
            double consumption = calculator.calculateConsumption(fuelAdded, currentOdo, prevOdo);

            System.out.println("\n=== ОТЧЕТ ПО ЗАПРАВКЕ ===");
            System.out.printf("Пробег между заправками: %d км%n", (currentOdo - prevOdo));
            System.out.printf("Залито топлива:         %.2f л%n", fuelAdded);
            System.out.printf("Цена за литр:           %.2f руб.%n", currentPrice);
            System.out.println("-------------------------");
            System.out.printf("Общая стоимость:        %.2f руб.%n", totalCost);
            System.out.printf("СРЕДНИЙ РАСХОД:         %.2f л/100 км%n", consumption);
        } else {
            System.out.println("\n[!] Не удалось рассчитать стоимость: цена на Аи-95 не найдена.");
        }
    }
}
