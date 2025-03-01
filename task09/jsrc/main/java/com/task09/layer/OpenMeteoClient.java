package com.task09.layer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class OpenMeteoClient {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    public HttpResponse<String> getWeather() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(weatherUrl))
                .build();
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}
