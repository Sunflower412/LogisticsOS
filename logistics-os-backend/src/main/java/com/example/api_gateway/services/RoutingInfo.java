package com.example.api_gateway.services;

import java.util.Collections;
import java.util.List;

public class RoutingInfo {
    private double distanceKm;     // расстояние в км
    private double timeMin;        // время в минутах
    private String geometry;       // закодированная линия маршрута
    private List<double[]> points; // список координат маршрута

    public RoutingInfo() {
        this.distanceKm = 0.0;
        this.timeMin = 0.0;
        this.geometry = null;
        this.points = Collections.emptyList();
    }

    public RoutingInfo(double distanceKm, double timeMin, String geometry, List<double[]> points) {
        this.distanceKm = distanceKm;
        this.timeMin = timeMin;
        this.geometry = geometry;
        this.points = points == null ? Collections.emptyList() : points;
    }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getTimeMin() { return timeMin; }
    public void setTimeMin(double timeMin) { this.timeMin = timeMin; }

    public String getGeometry() { return geometry; }
    public void setGeometry(String geometry) { this.geometry = geometry; }

    public List<double[]> getPoints() { return points; }
    public void setPoints(List<double[]> points) { this.points = points; }
}
