package com.example.pricebenz;

import org.json.JSONObject;

public class FuelReporter {

    public void printAzsInfo(FuelData data) {
        if (data.getAzsList() != null && data.getAzsList().length() > 0) {
            JSONObject azs = data.getAzsList().getJSONObject(0);
            String brand = azs.optJSONObject("brand") != null
                    ? azs.getJSONObject("brand").getString("name")
                    : "Частная АЗС";
            System.out.println("\n=== ИНФОРМАЦИЯ ОБ АЗС ===");
            System.out.println("Сеть:    " + brand);
            System.out.println("Адрес:   " + azs.optString("address", "Не указан"));
        }
    }

    public void printTripReport(Fuel fuel, double fuelAdded, int currentOdo, int prevOdo) {
        TripCalculator calc = new TripCalculator();

        // Вычисляем пройденный путь
        int distance = currentOdo - prevOdo;
        // Считаем расход
        double consumption = calc.calculateConsumption(fuelAdded, currentOdo, prevOdo);
        // Считаем стоимость
        double totalCost = calc.calculateTotalCost(fuelAdded, fuel.getPrice());

        System.out.println("\n=== ОТЧЕТ ПО ЗАПРАВКЕ ===");
        System.out.println("Топливо:    " + fuel.getName());
        System.out.println("Цена:       " + fuel.getPrice() + " " + fuel.getType());
        System.out.println("-------------------------");
        System.out.printf("Заправлено: %.2f л%n", fuelAdded);
        System.out.printf("Пройдено:   %d км%n", distance);
        System.out.printf("Расход:     %.2f л/100 км%n", consumption);
        System.out.println("-------------------------");
        System.out.printf("ИТОГО:      %.2f руб.%n", totalCost);
        System.out.println("=========================\n");
    }
}