package com.example.api_gateway.services;


import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoutingService {

    private final DriverRepository driverRepository;


    public RoutingService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver findBestDriverForOrder(Order order){
        List<Driver> availableDrivers = driverRepository.findAll();

        return availableDrivers.stream()
                .filter(driver -> driver.getExperience_coefficient_perMonth() >= 2.5)
                .max((d1, d2) -> d2)
    }
}
