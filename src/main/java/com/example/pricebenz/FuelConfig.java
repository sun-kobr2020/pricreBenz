package com.example.pricebenz;

import java.util.LinkedHashMap;
import java.util.Map;

public class FuelConfig {
    public static final String BASE_URL = "https://multigo.ru";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    // Координаты поиска
    public static final double LAT = 44.301719; //широту подставлять из даннх Tbox
    public static final double LNG = 38.711232; //долготу подставлять из данных Tbox

    // Карта отслеживаемого топлива, у каждого топлива есть своё ID, можно вывести настройку топлива опционально
    public static final Map<Integer, String> FUEL_MAP = new LinkedHashMap<>() {{
        put(8, "Аи-92");
        put(11, "Аи-95");
        put(14, "Аи-98");
        put(16, "Аи-100");
        put(3, "ДТ");
        put(18, "Метан/КПГ"); // Исправлено с 19 на 18 (по цене 28.62)
        put(1, "Газ (LPG)");
    }};
}