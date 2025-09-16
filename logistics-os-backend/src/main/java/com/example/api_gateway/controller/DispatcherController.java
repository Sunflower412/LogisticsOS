package com.example.api_gateway.controller;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.services.DispatcherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherController {

    private final DispatcherService dispatcherService;

    public DispatcherController(DispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @GetMapping("/notifications")
    public List<String> getNotifications() {
        return dispatcherService.getNotifications();
    }

    @DeleteMapping("/notifications")
    public ResponseEntity<String> clearNotifications() {
        dispatcherService.clearNotifications();
        return ResponseEntity.ok("Уведомления очищены");
    }

    @GetMapping("/orders/active")
    public List<Order> getActiveOrders() {
        return dispatcherService.getActiveOrders();
    }

    @GetMapping("/orders/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable OrderStatus status) {
        return dispatcherService.getOrdersByStatus(status);
    }

    @GetMapping("/drivers/available")
    public List<Driver> getAvailableDrivers() {
        return dispatcherService.findAvailableDrivers();
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignDriverToOrder(
            @RequestParam Long orderId,
            @RequestParam Long driverId) {

        boolean success = dispatcherService.assignDriverToOrder(orderId, driverId);

        if (success) {
            return ResponseEntity.ok("Водитель успешно назначен на заказ");
        } else {
            return ResponseEntity.badRequest().body("Ошибка при назначении водителя");
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<String> forceCompleteOrder(
            @RequestParam Long orderId,
            @RequestParam String reason) {

        boolean success = dispatcherService.forceCompleteOrder(orderId, reason);

        if (success) {
            return ResponseEntity.ok("Заказ принудительно завершен");
        } else {
            return ResponseEntity.badRequest().body("Ошибка при завершении заказа");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getDriverStats() {
        return ResponseEntity.ok(dispatcherService.getDriverStats());
    }
}
