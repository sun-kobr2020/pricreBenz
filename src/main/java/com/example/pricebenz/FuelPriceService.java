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
        // Упрощаем регулярку по совету IDE: \\D означает "не цифра"
        String digits = fuelName.replaceAll("\\D", "");
        for (int i = 0; i < fuels.length(); i++) {
            JSONObject f = fuels.getJSONObject(i);
            String label = f.optString("fuelId", "");
            if (f.optInt("fuelIdRaw") == targetId || (!digits.isEmpty() && label.contains(digits))) {
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
}