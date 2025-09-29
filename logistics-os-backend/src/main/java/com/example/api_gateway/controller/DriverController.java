package com.example.api_gateway.controller;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.repository.DriverRepository;
import com.example.api_gateway.services.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverRepository driverRepository;
    private final OrderService orderService;


    public DriverController(OrderService orderService, DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @GetMapping("/{driverId}/active-orders")
    public List<Order> getActiveOrders(@PathVariable Long driverId) {
        return orderService.getDriverActiveOrders(driverId);
    }

    // История заказов
    @GetMapping("/{driverId}/history")
    public List<Order> getDriverHistory(@PathVariable Long driverId) {
        return orderService.getDriverCompletedOrders(driverId);
    }
    @PostMapping
    public Driver createDriver(@RequestBody Driver driver){
        return driverRepository.save(driver);
    }
}