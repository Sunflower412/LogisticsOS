package com.example.api_gateway.services;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.repository.DriverRepository;
import com.example.api_gateway.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DispatcherService {

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final List<String> notifications = new ArrayList<>();

    public DispatcherService(OrderRepository orderRepository, DriverRepository driverRepository) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Отправка уведомления диспетчеру
     */
    public void sendNotification(String message) {
        String timestampedMessage = LocalDateTime.now() + " - " + message;
        notifications.add(0, timestampedMessage); // Добавляем в начало списка

        // Ограничиваем размер списка уведомлений (последние 100)
        if (notifications.size() > 100) {
            notifications.remove(notifications.size() - 1);
        }

        System.out.println("Уведомление диспетчеру: " + timestampedMessage);
    }

    /**
     * Получение всех уведомлений
     */
    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    /**
     * Очистка уведомлений
     */
    public void clearNotifications() {
        notifications.clear();
    }

    /**
     * Ручное назначение водителя на заказ
     */
    public boolean assignDriverToOrder(Long orderId, Long driverId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        Optional<Driver> driverOpt = driverRepository.findById(driverId);

        if (orderOpt.isPresent() && driverOpt.isPresent()) {
            Order order = orderOpt.get();
            Driver driver = driverOpt.get();

            order.setDriver(driver);
            order.setStatus(OrderStatus.ASSIGNED);
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            String message = String.format("Заказ #%d назначен водителю %s %s",
                    orderId, driver.getFirstname(), driver.getLastName());
            sendNotification(message);

            return true;
        }

        return false;
    }

    /**
     * Поиск доступных водителей для заказа
     */
    public List<Driver> findAvailableDrivers() {
        return driverRepository.findByActiveTrue();
    }

    /**
     * Получение заказов по статусу
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Получение всех активных заказов
     */
    public List<Order> getActiveOrders() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS));
    }

    /**
     * Принудительное завершение заказа
     */
    public boolean forceCompleteOrder(Long orderId, String reason) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(OrderStatus.valueOf(OrderStatus.COMPLETED_BY_DISPATCHER  + reason));
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            String message = String.format("Заказ #%d принудительно завершен. Причина: %s",
                    orderId, reason);
            sendNotification(message);

            return true;
        }

        return false;
    }

    /**
     * Получение статистики по водителям
     */
    public String getDriverStats() {
        List<Driver> drivers = driverRepository.findAll();
        long activeDrivers = drivers.stream().filter(Driver::getActive).count();
        long inactiveDrivers = drivers.size() - activeDrivers;

        return String.format("Активные водители: %d, Неактивные: %d", activeDrivers, inactiveDrivers);
    }
}