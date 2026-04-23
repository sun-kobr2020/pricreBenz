package com.example.pricebenz;

import java.util.Map;

public class FuelApp {
    private final FuelApiClient api = new FuelApiClient();
    private final FuelPriceService service = new FuelPriceService();
    private final FuelReporter reporter = new FuelReporter();

    /**
     * Основной рабочий цикл приложения
     */
    public void run(int targetId) throws Exception {
        // 1. Инициализация
        api.warmup();
        String targetName = FuelConfig.FUEL_MAP.getOrDefault(targetId, "Неизвестно");

        // 2. Получение данных
        FuelData data = service.fetchAllData(api, targetId);

        // 3. Вывод результатов
        reporter.printAzsInfo(data);
        printPriceSummary(data);
        processTrip(data, targetId, targetName);
    }

    private void printPriceSummary(FuelData data) {
        System.out.println("\n--- СВОДКА ЦЕН ПО ВСЕМ ТИПАМ ---");
        for (Map.Entry<Integer, String> entry : FuelConfig.FUEL_MAP.entrySet()) {
            Fuel fuel = service.getBestFuelData(data, entry.getKey(), entry.getValue(), false);
            if (fuel != null) System.out.println(fuel);
        }
    }

    private void processTrip(FuelData data, int id, String name) {
        Fuel target = service.getBestFuelData(data, id, name, true);
        if (target != null) {
            // Данные одометра и литры (имитация)
            reporter.printTripReport(target, 40.0, 50550, 50000, FuelConfig.LAT, FuelConfig.LNG);
        }
    }
}
