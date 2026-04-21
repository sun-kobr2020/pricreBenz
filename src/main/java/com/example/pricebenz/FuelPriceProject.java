package com.example.pricebenz;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FuelPriceProject {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Принудительная кодировка UTF-8 для вывода в консоль
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        // Настройка автоматического управления куками
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        // Создаем клиент с поддержкой HTTP/2 и авто-редиректами
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        // 1. ПРОГРЕВ: Заходим на главную для получения сессионных кук
        HttpRequest warmupRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://multigo.ru/"))
                .GET()
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        client.send(warmupRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Сессия инициализирована успешно.");

        // 2. ОСНОВНОЙ ЗАПРОС
        String apiUrl = "https://multigo.ru/api/9/near/list";
        int targetFuelId = 11; // ID искомого топлива (95+)
        double myLat = 55.753082;
        double myLng = 37.601043;

        String jsonPayload = String.format(Locale.US,
                "{\"limit\":6,\"fuelId\":%d,\"lat\":%.6f,\"lng\":%.6f}",
                targetFuelId, myLat, myLng
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .setHeader("Origin", "https://multigo.ru")
                .setHeader("Referer", "https://multigo.ru/")
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        // ОБРАБОТКА РЕЗУЛЬТАТА
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());

            if (jsonResponse.has("data")) {
                JSONArray list = jsonResponse.getJSONObject("data").getJSONArray("list");

                if (list.length() > 0) {
                    JSONObject azs = list.getJSONObject(0); // Ближайшая заправка
                    String brand = azs.getJSONObject("brand").getString("name");
                    String address = azs.getString("address");

                    // Поиск цены нужного топлива в массиве fuels
                    JSONArray fuels = azs.getJSONArray("fuels");
                    double fuelPrice = 0;
                    for (int i = 0; i < fuels.length(); i++) {
                        if (fuels.getJSONObject(i).optInt("fuelIdRaw") == targetFuelId || i == 0) {
                            fuelPrice = fuels.getJSONObject(i).getDouble("fuelPrice");
                        }
                    }

                    System.out.println("\n=== ДАННЫЕ АЗС ===");
                    System.out.println("Сеть:    " + brand);
                    System.out.println("Адрес:   " + address);
                    System.out.println("Топливо: Аи-95+");
                    System.out.println("Цена:    " + fuelPrice + " руб.");
                }
            }
        } else {
            System.err.println("Ошибка API (код " + response.statusCode() + "): " + response.body());
        }
    }
}
