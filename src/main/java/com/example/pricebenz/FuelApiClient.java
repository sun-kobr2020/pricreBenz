package com.example.pricebenz;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class FuelApiClient {
    private final HttpClient client;

    public FuelApiClient() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public void warmup() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FuelConfig.BASE_URL))
                .GET()
                .setHeader("User-Agent", FuelConfig.USER_AGENT)
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String sendPost(String endpoint, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FuelConfig.BASE_URL + endpoint))
                .timeout(java.time.Duration.ofSeconds(10)) // Тайм-аут на чтение
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .setHeader("Origin", FuelConfig.BASE_URL)
                .setHeader("Referer", FuelConfig.BASE_URL + "/")
                .setHeader("User-Agent", FuelConfig.USER_AGENT)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        // Проверка статуса ответа
        if (response.statusCode() != 200) {
            throw new IOException("Сервер вернул ошибку: " + response.statusCode());
        }
        return response.body();
    }
}