package com.example.api_gateway.services;


import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoutingService {

    private final DriverRepository driverRepository;

    public RoutingService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver findBestDriverForOrder(Order order) {
        // Временная реализация - выбираем водителя с лучшим рейтингом
        List<Driver> availableDrivers = driverRepository.findAll();

        return availableDrivers.stream()
                .filter(driver -> driver.getExperienceLevel() >= 2) // Минимальный опыт
                .max((d1, d2) -> d2.getRatingAllTime()
                        .compareTo(d1.getRatingAllTime()))
                .orElse(null);
    }

    public void assignOrderToDriver(Order order, Driver driver) {
        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);
        // Здесь будет логика уведомления водителя
    }
}