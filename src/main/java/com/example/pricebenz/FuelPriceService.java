package com.example.pricebenz;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Locale;

public class FuelPriceService {

    // Собирает данные из двух API в один объект FuelData
    public FuelData fetchAllData(FuelApiClient api, int targetFuelId) throws Exception {
        String listPayload = String.format(Locale.US, "{\"limit\":3,\"fuelId\":%d,\"lat\":%.6f,\"lng\":%.6f}",
                targetFuelId, FuelConfig.LAT, FuelConfig.LNG);
        String avgPayload = String.format(Locale.US, "{\"lat\":%.6f,\"lng\":%.6f}",
                FuelConfig.LAT, FuelConfig.LNG);

        JSONObject listJson = new JSONObject(api.sendPost("/api/9/near/list", listPayload));
        JSONObject avgJson = new JSONObject(api.sendPost("/api/9/avgprices", avgPayload));

        return new FuelData(listJson, avgJson);
    }

    // Универсальный поиск: сначала точная, если нет — средняя
    public double findPrice(FuelData data, int fuelId, String fuelName) {
        double price = 0;
        if (data.getAzsList() != null && data.getAzsList().length() > 0) {
            price = findExactPrice(data.getAzsList().getJSONObject(0), fuelId, fuelName);
        }
        if (price <= 0) {
            price = getAveragePrice(data.getAvgPricesMap(), fuelId);
        }
        return price;
    }

    public double findExactPrice(JSONObject azs, int targetId, String fuelName) {
        JSONArray fuels = azs.optJSONArray("fuels");
        if (fuels == null) return 0;

        String search = fuelName.toLowerCase();

        for (int i = 0; i < fuels.length(); i++) {
            JSONObject f = fuels.getJSONObject(i);
            String label = f.optString("fuelId", "").toLowerCase();
            int rawId = f.optInt("fuelIdRaw", -1);

            // 1. Сначала проверяем точный ID (если он есть в ответе)
            if (rawId == targetId) return f.optDouble("fuelPrice", 0);

            // 2. ГАЗ: СУГ (LPG / Пропан)
            if (search.contains("газ") || search.contains("lpg") || search.contains("суг") || search.contains("пропан")) {
                if (label.contains("суг") || label.contains("газ") || label.contains("пропан") || label.contains("lpg")) {
                    return f.optDouble("fuelPrice", 0);
                }
            }

            // 3. ГАЗ: КПГ (Метан / CNG)
            if (search.contains("метан") || search.contains("кпг") || search.contains("cng")) {
                if (label.contains("кпг") || label.contains("метан") || label.contains("cng")) {
                    return f.optDouble("fuelPrice", 0);
                }
            }

            // 4. ДИЗЕЛЬ: Дт, Дт+, Дизель
            if (search.contains("дт") || search.contains("диз")) {
                if (label.contains("дт") || label.contains("диз")) {
                    return f.optDouble("fuelPrice", 0);
                }
            }

            // 5. БЕНЗИНЫ: Поиск цифр (92, 95, 98, 100)
            // replaceAll("\\D", "") оставит только цифры из названия
            String searchDigits = fuelName.replaceAll("\\D", "");
            String labelDigits = label.replaceAll("\\D", "");

            if (!searchDigits.isEmpty() && !labelDigits.isEmpty() && searchDigits.equals(labelDigits)) {
                return f.optDouble("fuelPrice", 0);
            }
        }
        return 0;
    }


    public double getAveragePrice(JSONObject avgPricesMap, int targetId) {
        if (avgPricesMap == null) return 0;
        JSONObject item = avgPricesMap.optJSONObject(String.valueOf(targetId));
        return (item != null) ? item.optDouble("avg", 0) : 0;
    }

    public Fuel getBestFuelData(FuelData data, int id, String name) {
        double price = 0;
        String type = "";

        if (data.getAzsList() != null && data.getAzsList().length() > 0) {
            price = findExactPrice(data.getAzsList().getJSONObject(0), id, name);
            if (price > 0) type = "[Точная]";
        }

        if (price <= 0) {
            price = getAveragePrice(data.getAvgPricesMap(), id);
            if (price > 0) type = "[Средняя]";
        }

        return (price > 0) ? new Fuel(name, price, type) : null;
    }

}