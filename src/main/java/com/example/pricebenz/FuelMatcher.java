package com.example.pricebenz;

public class FuelMatcher {
    public static boolean isMatch(String label, String searchName) {
        String cleanLabel = label.toLowerCase().trim().replace(" ", "");
        String cleanSearch = searchName.toLowerCase().trim().replace(" ", "");

        // 1. Проверка на соответствие "плюса"
        if (cleanSearch.contains("+") != cleanLabel.contains("+")) return false;

        // 2. Логика для Дизеля
        if (cleanSearch.contains("дт") || cleanSearch.contains("диз")) {
            return cleanLabel.contains("дт") || cleanLabel.contains("диз");
        }

        // 3. Логика для Газов
        if (cleanSearch.contains("суг") || cleanSearch.contains("кпг")) {
            return cleanLabel.contains(cleanSearch.substring(0, 3));
        }

        // 4. Логика для Бензинов (сравнение цифр)
        String sDigits = cleanSearch.replaceAll("\\D", "");
        String lDigits = cleanLabel.replaceAll("\\D", "");
        return !sDigits.isEmpty() && sDigits.equals(lDigits);
    }
}
