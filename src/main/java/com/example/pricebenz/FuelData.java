package com.example.pricebenz;

import org.json.JSONArray;
import org.json.JSONObject;

public class FuelData {
    private final JSONArray azsList;
    private final JSONObject avgPricesMap;

    public FuelData(JSONObject listJson, JSONObject avgJson) {
        JSONObject dataList = listJson.optJSONObject("data");
        this.azsList = (dataList != null) ? dataList.optJSONArray("list") : null;

        JSONObject dataAvg = avgJson.optJSONObject("data");
        this.avgPricesMap = (dataAvg != null) ? dataAvg.optJSONObject("avgprice") : null;
    }

    public JSONArray getAzsList() { return azsList; }
    public JSONObject getAvgPricesMap() { return avgPricesMap; }
}