package com.example.pricebenz;

import java.util.LinkedHashMap;
import java.util.Map;

public class FuelConfig {
    public static final String BASE_URL = "https://multigo.ru";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    // Координаты поиска
    public static final double LAT = 44.211484; //широту подставлять из даннх Tbox
    public static final double LNG = 38.888887; //долготу подставлять из данных Tbox

    // Карта отслеживаемого топлива
    public static final Map<Integer, String> FUEL_MAP = new LinkedHashMap<>() {{
        put(8, "Аи-92");
        put(11, "Аи-95");
        put(14, "Аи-98");
        put(16, "Аи-100");
        put(17, "ДТ");
        put(19, "Газ (LPG)");
    }};
}