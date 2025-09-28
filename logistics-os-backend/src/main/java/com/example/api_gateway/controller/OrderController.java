package com.example.api_gateway.controller;

import com.example.api_gateway.dto.OrderDTO;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.services.OrderService;
import com.example.api_gateway.services.RoutingService;
import com.example.api_gateway.util.MapperUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "/api/orders", produces = "application/json")
public class OrderController {

    private final OrderService orderService;
    private final RoutingService routingService;

    public OrderController(OrderService orderService, RoutingService routingService) {
        this.orderService = orderService;
        this.routingService = routingService;
    }

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(MapperUtil::toOrderDTO)
                .toList();
    }

    @GetMapping("/active")
    public List<Order> getActiveOrders() {
        return orderService.getActiveOrders();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }

    @GetMapping("/driver/{driverId}")
    public List<Order> getDriverOrders(@PathVariable Long driverId) {
        return orderService.getDriversOrders(driverId);
    }

    // Основной метод создания заказа
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating order: " + e.getMessage());
        }
    }


    // Назначение конкретного водителя на заказ
    @PostMapping("/{orderId}/assign-driver")
    public ResponseEntity<String> assignDriverToOrder(
            @PathVariable Long orderId,
            @RequestParam Long driverId) {
        try {
            Order order = orderService.assignDriverToOrder(orderId, driverId);
            return ResponseEntity.ok("Driver assigned successfully to order");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Завершение заказа через параметры URL
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<String> completeOrder(
            @PathVariable Long orderId,
            @RequestParam boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime actualDeliveryTime) {

        System.out.println("✅ Received completion request for order: " + orderId);
        System.out.println("Success: " + success);
        System.out.println("Actual delivery time: " + actualDeliveryTime);

        try {
            // Если время не указано, используем текущее
            LocalDateTime deliveryTime = actualDeliveryTime != null ?
                    actualDeliveryTime : LocalDateTime.now();

            Order completedOrder = orderService.completeOrder(orderId, success, deliveryTime);
            return ResponseEntity.ok("Order completed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Обновление статуса заказа
    @PostMapping("/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            Order order = orderService.updateOrderStatus(orderId, orderStatus);
            return ResponseEntity.ok("Order status updated to: " + status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Провал заказа с причиной
    @PostMapping("/{orderId}/fail")
    public ResponseEntity<String> failOrder(
            @PathVariable Long orderId,
            @RequestParam String reason) {
        try {
            Order failedOrder = orderService.failOrder(orderId, reason);
            return ResponseEntity.ok("Order failed: " + reason);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/suggest-driver")
    public ResponseEntity<Long> suggestDriver(@PathVariable Long orderId) {
        try {
            // Заглушка - можно добавить логику выбора водителя
            return ResponseEntity.ok(1L);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(-1L);
        }
    }

    // Дополнительные GET методы
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(MapperUtil::toOrderDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/completed")
    public List<Order> getCompletedOrders() {
        return orderService.getCompletedOrders();
    }
}