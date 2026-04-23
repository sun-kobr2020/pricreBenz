package com.example.pricebenz;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Locale;

public class FuelPriceService {

    public FuelData fetchAllData(FuelApiClient api, int targetFuelId) throws Exception {
        int apiId = FuelIdMapper.getBaseId(targetFuelId);

        String listPayload = String.format(Locale.US, "{\"limit\":3,\"fuelId\":%d,\"lat\":%.6f,\"lng\":%.6f}",
                apiId, FuelConfig.LAT, FuelConfig.LNG);
        String avgPayload = String.format(Locale.US, "{\"lat\":%.6f,\"lng\":%.6f}",
                FuelConfig.LAT, FuelConfig.LNG);

        return new FuelData(
                new JSONObject(api.sendPost("/api/9/near/list", listPayload)),
                new JSONObject(api.sendPost("/api/9/avgprices", avgPayload))
        );
    }

    public Fuel getBestFuelData(FuelData data, int id, String name, boolean showWarning) {
        double price = findExactPriceInAzs(data, id, name);
        String type = (price > 0) ? "[Точная]" : "";

        if (price <= 0) {
            if (showWarning) checkPlusVersionAvailability(data, id, name);
            price = getAveragePrice(data.getAvgPricesMap(), id);
            type = (price > 0) ? "[Средняя]" : "";
        }

        return (price > 0) ? new Fuel(name, price, type) : null;
    }

    private double findExactPriceInAzs(FuelData data, int id, String name) {
        if (data.getAzsList() == null || data.getAzsList().length() == 0) return 0;
        JSONArray fuels = data.getAzsList().getJSONObject(0).optJSONArray("fuels");
        if (fuels == null) return 0;

        for (int i = 0; i < fuels.length(); i++) {
            JSONObject f = fuels.getJSONObject(i);
            if (f.optInt("fuelIdRaw") == id || FuelMatcher.isMatch(f.optString("fuelId"), name)) {
                return f.optDouble("fuelPrice", 0);
            }
        }
        return 0;
    }

    private void checkPlusVersionAvailability(FuelData data, int id, String name) {
        boolean isPlusRequested = name.contains("+");
        int altId = isPlusRequested ? FuelIdMapper.getBaseId(id) : FuelIdMapper.getPlusId(id);
        String altName = isPlusRequested ? name.replace("+", "") : name + "+";

        if (findExactPriceInAzs(data, altId, altName) > 0) {
            System.out.printf("[!] На данной АЗС найден только %s, для %s используется средняя цена.%n",
                    altName, isPlusRequested ? "премиального" : "обычного");
        }
    }

    public double getAveragePrice(JSONObject avgMap, int id) {
        if (avgMap == null) return 0;
        int baseId = (id > 100) ? id / 10 : id; // на случай виртуальных ID
        JSONObject item = avgMap.optJSONObject(String.valueOf(baseId));
        return (item != null) ? item.optDouble("avg", 0) : 0;
    }
}
