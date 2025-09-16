package com.example.api_gateway.services;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final DriverRatingService driverRatingService;


    public OrderService(OrderRepository orderRepository, DriverRatingService driverRatingService) {
        this.orderRepository = orderRepository;
        this.driverRatingService = driverRatingService;
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        // Автоматически назначаем подходящего водителя
        Driver suitableDriver = driverRatingService.findSuitableDriver(order);
        if (suitableDriver != null) {
            order.setDriver(suitableDriver);
            order.setStatus(OrderStatus.ASSIGNED);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.valueOf(status));
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order completeOrder(Long orderId, boolean success, LocalDateTime actualDeliveryTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setActualDeliveryTime(actualDeliveryTime);
        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Обновляем рейтинг водителя
        driverRatingService.updateDriverRating(savedOrder);

        return savedOrder;
    }

    @Transactional
    public Order failOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.valueOf(OrderStatus.FAILED + reason));
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Обновляем рейтинг водителя (штраф за проваленный заказ)
        driverRatingService.updateDriverRating(savedOrder);

        return savedOrder;
    }

    public List<Order> getOrdersByStatus(OrderStatus status){
        return orderRepository.findByStatus(status);
    }

    public List<Order> getDriversOrders(Long driverId){
        return orderRepository.findByDriverId(driverId);
    }

    public List<Order> getActiveOrders(){
        return orderRepository.findByStatusIn(List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS));
    }
}
