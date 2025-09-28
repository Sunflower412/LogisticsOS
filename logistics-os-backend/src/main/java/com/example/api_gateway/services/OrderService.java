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
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final DriverRatingService driverRatingService;
    private final RabbitMQService rabbitMQService;

    public OrderService(OrderRepository orderRepository,
                        DriverRepository driverRepository,
                        DriverRatingService driverRatingService,
                        RabbitMQService rabbitMQService) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.driverRatingService = driverRatingService;
        this.rabbitMQService = rabbitMQService;
    }

    /* ---------------------- Read methods ---------------------- */

    public List<Order> getActiveOrders() {
        return orderRepository.findByStatusIn(List.of(
                OrderStatus.CREATED,
                OrderStatus.ASSIGNED,
                OrderStatus.IN_PROGRESS
        ));
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getDriversOrders(Long driverId) {
        return orderRepository.findByDriverId(driverId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getCompletedOrders() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.DELIVERED, OrderStatus.FAILED));
    }

    public List<Order> getOrdersByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public List<Order> getOrdersByCustomer(String customerPhone) {
        return orderRepository.findByClientPhone(customerPhone);
    }

    /* ---------------------- Create / Assign ---------------------- */

    /**
     * Создаёт заказ и пытается автоматически назначить подходящего водителя.
     * Сохраняем заказ сначала, чтобы получить ID (важно для событий).
     */
    @Transactional
    public Order createOrder(Order order) {
        // init
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setStatus(OrderStatus.CREATED);

        // сохраняем заказ чтобы получить id для событий
        Order saved = orderRepository.save(order);

        // пробуем найти подходящего водителя
        Driver suitableDriver = driverRatingService.findSuitableDriver(saved);

        if (suitableDriver != null) {
            try {
                // назначаем и сохраняем снова
                saved.setDriver(suitableDriver);
                saved.setStatus(OrderStatus.ASSIGNED);
                saved.setUpdatedAt(LocalDateTime.now());

                Order finalSaved = orderRepository.save(saved);

                // синхронизируем в памяти коллекцию водителя (не обязательно для БД, но полезно)
                syncDriverOrdersCollection(suitableDriver, finalSaved);

                // отправляем события
                rabbitMQService.sendNewOrderEvent(finalSaved.getId());
                rabbitMQService.sendDriverAssignedEvent(finalSaved.getId(), suitableDriver.getId());

                return finalSaved;
            } catch (Exception e) {
                // в случае ошибки логируем через rabbit и возвращаем заказ без назначения
                rabbitMQService.sendNotification("Ошибка при автоназначении водителя: " + e.getMessage());
                return saved;
            }
        } else {
            // нет подходящего водителя — отправляем только событие создания
            rabbitMQService.sendNewOrderEvent(saved.getId());
            return saved;
        }
    }

    /**
     * Назначает конкретного водителя на заказ.
     * Проверяет состояние заказа и пригодность водителя.
     */
    @Transactional
    public Order assignDriverToOrder(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.ASSIGNED) {
            // можно варьировать условие по логике — сейчас не даём переназначать выполняющиеся/завершённые
            if (order.getStatus() == OrderStatus.IN_PROGRESS || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.FAILED) {
                throw new RuntimeException("Нельзя назначить водителя для заказа в статусе: " + order.getStatus());
            }
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Водитель не найден: " + driverId));

        if (!Boolean.TRUE.equals(driver.getActive())) {
            throw new RuntimeException("Водитель неактивен: " + driver.getFirstname() + " " + driver.getLastName());
        }

        if (driver.getRatingAllTime() != null && driver.getRatingAllTime().compareTo(BigDecimal.valueOf(2.5)) < 0) {
            throw new RuntimeException("Рейтинг водителя слишком низкий для назначения");
        }

        // Выполняем привязку — set на заказ (владелец связи) и синхронизация коллекции у водителя
        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order); // сохраняем owner — это обновит FK driver_id

        // синхронизируем коллекцию в памяти, чтобы driver.getOrders() отражал изменение
        syncDriverOrdersCollection(driver, saved);

        // уведомления/события
        rabbitMQService.sendDriverAssignedEvent(saved.getId(), driver.getId());
        rabbitMQService.sendNotification(String.format("✅ Водитель %s %s (рейтинг: %s) назначен на заказ #%d",
                driver.getFirstname(), driver.getLastName(),
                driver.getRatingAllTime() != null ? driver.getRatingAllTime().toString() : "N/A",
                saved.getId()));

        return saved;
    }

    /* ---------------------- Status updates ---------------------- */

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        if (oldStatus != status) {
            rabbitMQService.sendNotification(String.format("Статус заказа #%d изменен: %s → %s",
                    orderId, oldStatus, status));
        }

        return saved;
    }

    @Transactional
    public Order completeOrder(Long orderId, boolean success, LocalDateTime actualDeliveryTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setActualDeliveryTime(actualDeliveryTime);
        order.setCompletedSuccessfully(success);
        order.setStatus(success ? OrderStatus.DELIVERED : OrderStatus.FAILED);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        // обновляем рейтинг через сервис
        driverRatingService.updateDriverRating(saved);

        if (order.getDriver() != null) {
            rabbitMQService.sendOrderCompletedEvent(orderId, order.getDriver().getId(), success);
        }

        String resultMessage = success ? "успешно завершен" : "завершен с ошибкой";
        rabbitMQService.sendNotification(String.format("Заказ #%d %s. Водитель: %s %s",
                orderId, resultMessage,
                order.getDriver() != null ? order.getDriver().getFirstname() : "-",
                order.getDriver() != null ? order.getDriver().getLastName() : "-"));

        return saved;
    }

    @Transactional
    public Order failOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.FAILED);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        driverRatingService.updateDriverRating(saved);

        rabbitMQService.sendNotification(String.format("Заказ #%d провален. Причина: %s", orderId, reason));

        if (order.getDriver() != null) {
            rabbitMQService.sendOrderCompletedEvent(orderId, order.getDriver().getId(), false);
        }

        return saved;
    }

    /* ---------------------- Helpers ---------------------- */

    /**
     * Синхронизирует коллекцию orders у Driver в памяти — добавляет заказ если его там нет.
     * Не производит лишних сохранений (в большинстве случаев не нужен explicit save(driver)).
     */
    private void syncDriverOrdersCollection(Driver driver, Order order) {
        if (driver == null || order == null) return;
        if (driver.getOrders() == null) {
            driver.setOrders(List.of(order));
            return;
        }
        boolean contains = driver.getOrders().stream()
                .anyMatch(o -> o.getId() != null && o.getId().equals(order.getId()));
        if (!contains) {
            driver.getOrders().add(order);
        }
    }
}
