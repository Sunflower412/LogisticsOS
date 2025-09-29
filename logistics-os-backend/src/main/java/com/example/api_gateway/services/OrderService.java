package com.example.api_gateway.services;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.repository.DriverRepository;
import com.example.api_gateway.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final DriverRatingService driverRatingService;
    private final RabbitMQService rabbitMQService;

    public OrderService(OrderRepository orderRepository, DriverRepository driverRepository,
                        DriverRatingService driverRatingService,
                        RabbitMQService rabbitMQService) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.driverRatingService = driverRatingService;
        this.rabbitMQService = rabbitMQService;
    }

    // Существующие методы...

    // Добавляем недостающие методы

    /**
     * Получить все активные заказы (CREATED, ASSIGNED, IN_PROGRESS)
     */
    public List<Order> getActiveOrders() {
        return orderRepository.findByStatusIn(List.of(
                OrderStatus.CREATED,
                OrderStatus.ASSIGNED,
                OrderStatus.IN_PROGRESS
        ));
    }

    /**
     * Получить заказы по статусу
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Получить заказы конкретного водителя
     */
    public List<Order> getDriversOrders(Long driverId) {
        return orderRepository.findByDriverId(driverId);
    }

    /**
     * Получить все заказы
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Получить заказ по ID
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Получить завершенные заказы
     */
    public List<Order> getCompletedOrders() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.DELIVERED, OrderStatus.FAILED));
    }

    /**
     * Получить заказы за период
     */
    public List<Order> getOrdersByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Получить заказы по клиенту
     */
    public List<Order> getOrdersByCustomer(String customerPhone) {
        // Используем JPQL запрос вместо производного метода
        return orderRepository.findByClientPhone(customerPhone);

        // Или альтернатива - получить все и отфильтровать на уровне сервиса
        // return orderRepository.findAll().stream()
        //        .filter(order -> order.getClient() != null &&
        //               customerPhone.equals(order.getClient().getPhone()))
        //        .collect(Collectors.toList());
    }

    public List<Order> getDriverActiveOrders(Long driverId) {
        return orderRepository.findByDriverIdAndStatusIn(
                driverId,
                Arrays.asList(OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS)
        );
    }
    public List<Order> getDriverCompletedOrders(Long driverId) {
        return orderRepository.findByDriverIdAndStatusIn(driverId, List.of(OrderStatus.DELIVERED, OrderStatus.FAILED));
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

            // Отправляем событие о назначении водителя
            rabbitMQService.sendDriverAssignedEvent(order.getId(), suitableDriver.getId());
        }

        Order savedOrder = orderRepository.save(order);

        // Отправляем событие о создании нового заказа
        rabbitMQService.sendNewOrderEvent(savedOrder.getId());

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Отправляем событие об изменении статуса
        if (oldStatus != status) {
            rabbitMQService.sendNotification(
                    String.format("Статус заказа #%d изменен: %s → %s",
                            orderId, oldStatus, status)
            );
        }

        return savedOrder;
    }

    @Transactional
    public Order completeOrder(Long orderId, boolean success, LocalDateTime actualDeliveryTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setActualDeliveryTime(actualDeliveryTime);
        order.setCompletedSuccessfully(success);
        order.setStatus(success ? OrderStatus.DELIVERED : OrderStatus.FAILED);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Обновляем рейтинг водителя
        driverRatingService.updateDriverRating(savedOrder);

        // Отправляем событие о завершении заказа
        if (order.getDriver() != null) {
            rabbitMQService.sendOrderCompletedEvent(
                    orderId,
                    order.getDriver().getId(),
                    success
            );
        }

        // Уведомление диспетчера
        String resultMessage = success ? "успешно завершен" : "завершен с ошибкой";
        rabbitMQService.sendNotification(
                String.format("Заказ #%d %s. Водитель: %s %s",
                        orderId, resultMessage,
                        order.getDriver().getFirstname(),
                        order.getDriver().getLastName())
        );

        return savedOrder;
    }

    @Transactional
    public Order failOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.FAILED);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Обновляем рейтинг водителя (штраф за проваленный заказ)
        driverRatingService.updateDriverRating(savedOrder);

        // Отправляем уведомление о провале заказа
        rabbitMQService.sendNotification(
                String.format("Заказ #%d провален. Причина: %s", orderId, reason)
        );

        if (order.getDriver() != null) {
            rabbitMQService.sendOrderCompletedEvent(orderId, order.getDriver().getId(), false);
        }

        return savedOrder;
    }

    @Transactional
    public Order assignDriverToOrder(Long orderId, Long driverId) {
        try {
            // Находим заказ
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Заказ не найден: " + orderId));

            // Проверяем, что заказ можно назначить
            if (order.getStatus() != OrderStatus.CREATED) {
                throw new RuntimeException("Заказ уже назначен или выполняется: " + orderId);
            }

            // Находим водителя
            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new RuntimeException("Водитель не найден: " + driverId));

            // Проверяем водителя
            if (!driver.getActive()) {
                throw new RuntimeException("Водитель неактивен: " + driver.getFirstname() + " " + driver.getLastName());
            }

            if (driver.getRatingAllTime().compareTo(new BigDecimal("2.5")) < 0) {
                throw new RuntimeException("Рейтинг водителя слишком низкий для назначения");
            }

            // Назначаем водителя
            order.setDriver(driver);
            order.setStatus(OrderStatus.ASSIGNED);
            order.setUpdatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            // Отправляем уведомления
            rabbitMQService.sendDriverAssignedEvent(orderId, driverId);
            rabbitMQService.sendNotification(
                    String.format("✅ Водитель %s %s (рейтинг: %.2f) назначен на заказ #%d",
                            driver.getFirstname(),
                            driver.getLastName(),
                            driver.getRatingAllTime().doubleValue(),
                            orderId)
            );

            return savedOrder;

        } catch (RuntimeException e) {
            // Логируем ошибку и отправляем уведомление
            rabbitMQService.sendNotification("❌ Ошибка назначения водителя: " + e.getMessage());
            throw e;
        }
    }
}