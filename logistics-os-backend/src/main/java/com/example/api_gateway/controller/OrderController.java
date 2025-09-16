package com.example.api_gateway.controller;

import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.services.OrderService;
import com.example.api_gateway.services.RoutingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final RoutingService routingService;

    public OrderController(OrderService orderService, RoutingService routingService) {
        this.orderService = orderService;
        this.routingService = routingService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getActiveOrders();
    }

    @GetMapping("/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable String status) {
        return orderService.getOrdersByStatus(OrderStatus.valueOf(status));
    }

    @GetMapping("/driver/{driverId}")
    public List<Order> getDriverOrders(@PathVariable Long driverId) {
        return orderService.getDriversOrders(driverId);
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PostMapping("/{orderId}/assign")
    public ResponseEntity<String> assignOrder(@PathVariable Long orderId) {
        try {
            Order order = orderService.updateOrderStatus(orderId, "ASSIGNED");
            return ResponseEntity.ok("Order assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<String> completeOrder(
            @PathVariable Long orderId,
            @RequestParam boolean success,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime actualDeliveryTime) {

        try {
            orderService.completeOrder(orderId, success, actualDeliveryTime);
            return ResponseEntity.ok("Order completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/suggest-driver")
    public ResponseEntity<Long> suggestDriver(@PathVariable Long orderId) {
        // Заглушка - будет интегрировано с маршрутизацией
        return ResponseEntity.ok(1L); // Возвращаем ID первого водителя
    }
}