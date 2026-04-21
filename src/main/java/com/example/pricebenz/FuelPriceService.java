package com.example.pricebenz;

import org.json.JSONArray;
import org.json.JSONObject;

public class FuelPriceService {

    /**
     * Пытается найти точную цену на конкретной АЗС
     */
    public double findExactPrice(JSONObject azs, int targetId, String fuelName) {
        JSONArray fuels = azs.optJSONArray("fuels");
        if (fuels == null) return 0;

        String digits = fuelName.replaceAll("[^0-9]", "");

        for (int i = 0; i < fuels.length(); i++) {
            JSONObject f = fuels.getJSONObject(i);
            String label = f.optString("fuelId", "");

            if (f.optInt("fuelIdRaw") == targetId || (!digits.isEmpty() && label.contains(digits))) {
                return f.optDouble("fuelPrice", 0);
            }
        }
        return 0;
    }

    /**
     * Берет среднюю цену из общего JSON региона
     */
    public double getAveragePrice(JSONObject avgPricesMap, int targetId) {
        if (avgPricesMap == null) return 0;
        JSONObject item = avgPricesMap.optJSONObject(String.valueOf(targetId));
        return (item != null) ? item.optDouble("avg", 0) : 0;
    }
}