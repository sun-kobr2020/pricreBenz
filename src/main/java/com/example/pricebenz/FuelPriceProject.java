package com.example.pricebenz;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class FuelPriceProject {
    public static void main(String[] args) {
        // Настройка системного вывода
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        try {
            // Создаем и запускаем приложение
            FuelApp app = new FuelApp();
            app.run(12); // Выбери каким топливом заправлять

        } catch (Exception e) {
            System.err.println("[КРИТИЧЕСКАЯ ОШИБКА]: " + e.getMessage());
        }
    }
}
