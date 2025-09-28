package graph_hopper;

import com.google.gson.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class OpenRouteServiceRouter {
    private static final String API_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjA5ODc5ZWRlNDFjZjQ4Yzg5YzBjNmNmOWI3NDkxNmM0IiwiaCI6Im11cm11cjY0In0=";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        try {
            double[][] coordinates = {
                    {37.6173, 55.7558}, // Москва
                    {40.4080, 56.1302}  // Иваново
            };

            // Формируем JSON тело запроса
            JsonObject requestBody = new JsonObject();

            // Координаты в правильном формате
            JsonArray coordsArray = new JsonArray();
            for (double[] coord : coordinates) {
                JsonArray point = new JsonArray();
                point.add(coord[0]); // долгота
                point.add(coord[1]); // широта
                coordsArray.add(point);
            }
            requestBody.add("coordinates", coordsArray);

            // Дополнительные параметры
            requestBody.addProperty("preference", "recommended");
            requestBody.addProperty("units", "km");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            System.out.println("Отправляем запрос к OpenRouteService...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Статус ответа: " + response.statusCode());

            JsonObject output = new JsonObject();

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

                if (jsonResponse.has("routes") && jsonResponse.getAsJsonArray("routes").size() > 0) {
                    JsonObject route = jsonResponse.getAsJsonArray("routes").get(0).getAsJsonObject();
                    JsonObject summary = route.getAsJsonObject("summary");

                    // Расстояние в километрах (уже в км в ответе)
                    double distanceKm = summary.get("distance").getAsDouble();

                    // Время в секундах
                    double durationSeconds = summary.get("duration").getAsDouble();
                    double durationMinutes = durationSeconds / 60;

                    // Геометрия маршрута
                    String geometryEncoded = route.get("geometry").getAsString();

                    // Декодируем полилинию в массив координат
                    List<double[]> decodedPoints = decodePolyline(geometryEncoded);
                    JsonArray pointsArray = new JsonArray();
                    for (double[] point : decodedPoints) {
                        JsonArray coordArray = new JsonArray();
                        coordArray.add(point[0]); // долгота
                        coordArray.add(point[1]); // широта
                        pointsArray.add(coordArray);
                    }

                    JsonObject routeObj = new JsonObject();
                    routeObj.addProperty("distance_km", Math.round(distanceKm * 100.0) / 100.0);
                    routeObj.addProperty("time_min", Math.round(durationMinutes * 100.0) / 100.0);
                    routeObj.addProperty("geometry", geometryEncoded); // исходная закодированная строка
                    routeObj.add("points", pointsArray); // декодированные координаты

                    JsonArray routesArray = new JsonArray();
                    routesArray.add(routeObj);
                    output.add("routes", routesArray);

                    System.out.println("Успешно декодировано точек: " + decodedPoints.size());

                } else {
                    output.addProperty("error", "Нет доступных маршрутов в ответе API");
                }

            } else {
                output.addProperty("error", "Ошибка API: " + response.statusCode());
                output.addProperty("body", response.body());
            }

            // Вывод JSON
            System.out.println(GSON.toJson(output));

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для декодирования полилинии
    public static List<double[]> decodePolyline(String encoded) {
        List<double[]> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        double lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double[] point = new double[]{lng * 1e-5, lat * 1e-5};
            poly.add(point);
        }

        return poly;
    }
}