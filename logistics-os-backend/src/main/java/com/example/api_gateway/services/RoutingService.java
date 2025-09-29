package com.example.api_gateway.services;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.repository.DriverRepository;
import graph_hopper.OpenRouteServiceRouter;
import com.google.gson.*;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class RoutingService {

    private final DriverRepository driverRepository;

    private static final String API_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String API_KEY = "ТВОЙ_API_KEY"; // лучше вынести в application.yml

    public RoutingService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // === твои методы поиска водителей ===
    public Driver findBestDriverForOrder(Order order) {
        List<Driver> availableDrivers = driverRepository.findAll();
        return availableDrivers.stream()
                .filter(Driver::getActive)
                .filter(d -> d.getRatingMonthly().compareTo(java.math.BigDecimal.valueOf(2.5)) >= 0)
                .max(Comparator.comparing(Driver::getRatingMonthly))
                .orElse(null);
    }

    public Driver findOptimalDriverForOrder(Order order) {
        List<Driver> availableDrivers = driverRepository.findByActiveTrue();
        return availableDrivers.stream()
                .filter(d -> d.getRatingMonthly().compareTo(java.math.BigDecimal.valueOf(2.5)) >= 0)
                .min(Comparator.comparing(d -> calculateDistance(d, order)))
                .orElse(null);
    }

    private double calculateDistance(Driver driver, Order order) {
        return Math.random() * 10; // пока заглушка
    }

    // === новый метод ===
    public RoutingInfo getRoutingInfo(double[] fromCoords, double[] toCoords) {
        try {
            JsonObject requestBody = new JsonObject();
            JsonArray coordsArray = new JsonArray();

            JsonArray from = new JsonArray();
            from.add(fromCoords[0]); // lon
            from.add(fromCoords[1]); // lat
            coordsArray.add(from);

            JsonArray to = new JsonArray();
            to.add(toCoords[0]);
            to.add(toCoords[1]);
            coordsArray.add(to);

            requestBody.add("coordinates", coordsArray);
            requestBody.addProperty("preference", "recommended");
            requestBody.addProperty("units", "km");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Ошибка API: " + response.statusCode() + " -> " + response.body());
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject route = jsonResponse.getAsJsonArray("routes").get(0).getAsJsonObject();
            JsonObject summary = route.getAsJsonObject("summary");

            double distanceKm = summary.get("distance").getAsDouble();
            double durationMin = summary.get("duration").getAsDouble() / 60.0;
            String geometry = route.get("geometry").getAsString();

            List<double[]> decodedPoints = OpenRouteServiceRouter.decodePolyline(geometry);

            return new RoutingInfo(distanceKm, durationMin, geometry, decodedPoints);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
