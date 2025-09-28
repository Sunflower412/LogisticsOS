//package graph_hopper;
//
//import com.google.gson.*;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Instant;
//
// * Пример Java-класса для построения маршрута с использованием Yandex Routing API.
// * API учитывает пробки и другие дорожные обстоятельства (трафик, прогноз на основе времени отправления).
// * Обрабатывает несколько альтернативных маршрутов.
// * Выводит результат в формате JSON с расстоянием, временем и всеми промежуточными точками (координатами) для каждого маршрута.
// *
// * Требования:
// * - Получите API-ключ на https://developer.tech.yandex.com/
// * - Запустите с аргументами: lonA latA lonB latB apiKey
// *   Пример: 37.6173 55.7558 40.4080 56.1302 your_api_key
// *
// * Вывод: JSON объект с массивом маршрутов, каждый содержит distance_km, time_min и points (массив [lon, lat]).
// */
//public class YandexRouter {
//
//    private static final String API_URL = "https://api.routing.yandex.net/v2/route";
//    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
//
//    public static void main(String[] args) {
////        if (args.length != 4) {
////            System.out.println("Usage: java YandexRouter lonA latA lonB latB apiKey");
////            return;
////        }
//
//        double lonA = Double.parseDouble("37.6173");
//        double latA = Double.parseDouble("55.7558");
//        double lonB = Double.parseDouble("40.4080");
//        double latB = Double.parseDouble("56.1302");
//        String apiKey = "6e04de65-7664-4f01-a543-43c0bb032d31";
//
//        try {
//            String waypoints = lonA + "," + latA + "|" + lonB + "," + latB;
//            long departureTime = Instant.now().getEpochSecond() + 1800; // +30 минут для прогноза трафика
//
//            String url = API_URL + "?waypoints=" + waypoints +
//                    "&mode=driving" +
//                    "&departure_time=" + departureTime +
//                    "&apikey=" + apiKey;
//
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(url))
//                    .header("Accept", "application/json")
//                    .build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            JsonObject output = new JsonObject();
//            JsonArray outputRoutes = new JsonArray();
//
//            if (response.statusCode() == 200) {
//                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
//                JsonArray routes = jsonResponse.getAsJsonArray("routes");
//                if (routes != null && routes.size() > 0) {
//                    for (int i = 0; i < routes.size(); i++) {
//                        JsonObject route = routes.get(i).getAsJsonObject();
//                        JsonArray legs = route.getAsJsonArray("legs");
//                        if (legs != null && legs.size() > 0) {
//                            JsonObject firstLeg = legs.get(0).getAsJsonObject();
//
//                            // Расстояние (в метрах)
//                            JsonObject distanceObj = firstLeg.getAsJsonObject("distance");
//                            double distanceMeters = distanceObj.get("value").getAsDouble();
//                            double distanceKm = distanceMeters / 1000;
//
//                            // Время с учетом трафика (в секундах)
//                            JsonObject durationObj = firstLeg.getAsJsonObject("duration_in_traffic");
//                            if (durationObj == null) {
//                                durationObj = firstLeg.getAsJsonObject("duration"); // Fallback, если нет трафика
//                            }
//                            double durationSeconds = durationObj.get("value").getAsDouble();
//                            double durationMinutes = durationSeconds / 60;
//
//                            // Сбор всех промежуточных точек из steps.polyline.points
//                            JsonArray steps = firstLeg.getAsJsonArray("steps");
//                            JsonArray points = new JsonArray();
//                            if (steps != null) {
//                                for (JsonElement stepElem : steps) {
//                                    JsonObject step = stepElem.getAsJsonObject();
//                                    JsonObject polyline = step.getAsJsonObject("polyline");
//                                    if (polyline != null) {
//                                        JsonArray stepPoints = polyline.getAsJsonArray("points");
//                                        if (stepPoints != null) {
//                                            for (JsonElement pointElem : stepPoints) {
//                                                points.add(pointElem);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            // Создание объекта для маршрута
//                            JsonObject routeObj = new JsonObject();
//                            routeObj.addProperty("distance_km", distanceKm);
//                            routeObj.addProperty("time_min", durationMinutes);
//                            routeObj.add("points", points);
//
//                            outputRoutes.add(routeObj);
//                        }
//                    }
//                    output.add("routes", outputRoutes);
//                } else {
//                    output.addProperty("error", "Нет доступных маршрутов.");
//                }
//            } else {
//                output.addProperty("error", "Ошибка API: " + response.statusCode());
//                output.addProperty("body", response.body());
//            }
//
//            // Вывод JSON
//            System.out.println(GSON.toJson(output));
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            System.err.println("Ошибка парсинга: " + e.getMessage());
//        }
//    }
//}