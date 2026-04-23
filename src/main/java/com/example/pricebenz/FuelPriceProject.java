package com.example.pricebenz;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Главный класс проекта (Точка входа).
 * Координирует работу сетевого клиента, сервиса цен и формирования отчетов.
 */
public class FuelPriceProject {

    public static void main(String[] args) {
        try {
            // 1. НАСТРОЙКА ОКРУЖЕНИЯ
            // Устанавливаем UTF-8 для корректного отображения кириллицы в консоли
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

            // Инициализация компонентов
            FuelApiClient api = new FuelApiClient();
            FuelPriceService service = new FuelPriceService();
            FuelReporter reporter = new FuelReporter();

            // Инициализация сессии (получение кук для обхода защиты сайта)
            api.warmup();

            // 2. ПОЛУЧЕНИЕ ДАННЫХ
            // Выбираем ID топлива для приоритетного поиска (например, 11 для Аи-95)
            int targetFuelId = 8;
            String targetFuelName = FuelConfig.FUEL_MAP.get(targetFuelId);

            // fetchAllData делает сразу два запроса к API (список АЗС и средние цены)
            FuelData data = service.fetchAllData(api, targetFuelId);

            // 3. ФОРМИРОВАНИЕ ВЫВОДА

            // Выводим информацию о ближайшей АЗС
            reporter.printAzsInfo(data);

            // Выводим сводную таблицу по всем видам топлива из конфигурации
            System.out.println("\n--- СВОДКА ЦЕН ПО ВСЕМ ТИПАМ ---");
            for (var entry : FuelConfig.FUEL_MAP.entrySet()) {
                // Метод getBestFuelData сам выбирает между [Точной] и [Средней] ценой
                Fuel fuel = service.getBestFuelData(data, entry.getKey(), entry.getValue(), false);
                if (fuel != null) {
                    System.out.println(fuel); // Используется переопределенный метод Fuel.toString()
                }
            }

            // 4. РАСЧЕТ ПОЕЗДКИ
            // Имитируем ситуацию: проехали 550 км (с 50000 до 50550) и залили 40 литров
            Fuel targetFuel = service.getBestFuelData(data, targetFuelId, targetFuelName, true);
            if (targetFuel != null) {
                // Reporter берет на себя всю математику через TripCalculator и вывод чека
                reporter.printTripReport(targetFuel, 40.0, 50550, 50000);
            }

        } catch (Exception e) {
            // Глобальная обработка ошибок (сеть, парсинг JSON и т.д.)
            System.err.println("[ОШИБКА] Произошел сбой в работе программы: " + e.getMessage());
            e.printStackTrace();
        }
    }
}