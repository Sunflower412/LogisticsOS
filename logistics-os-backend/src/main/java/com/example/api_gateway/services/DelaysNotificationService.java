package com.example.api_gateway.services;

import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DelaysNotificationService {
    private final OrderRepository orderRepository;
    private final DispatcherService dispatcherService;

    public DelaysNotificationService(OrderRepository orderRepository, DispatcherService dispatcherService) {
        this.orderRepository = orderRepository;
        this.dispatcherService = dispatcherService;
    }

    // Проверяем возможные опоздания каждые 30 минут
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void checkForPotentialDelays() {
        List<Order> activeOrders = orderRepository.findByStatusIn(
                List.of(OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS)
        );

        for (Order order : activeOrders) {
            if (isAtRiskOfDelay(order)) {
                notifyDispatcher(order);
            }
        }
    }

    private boolean isAtRiskOfDelay(Order order) {
        if (order.getPlannedDeliveryTime() == null || order.getDriver() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration timeRemaining = Duration.between(now, order.getPlannedDeliveryTime());
        long minutesRemaining = timeRemaining.toMinutes();

        // Если осталось меньше времени чем нужно на доставку + 30% запаса
        // Здесь должна быть более сложная логика расчета времени доставки
        return minutesRemaining < 90; // Эмпирическое значение
    }

    private void notifyDispatcher(Order order) {
        String message = String.format(
                "Водитель %s %s (рейтинг: %.2f) может не успеть доставить заказ #%d к %s. Рекомендуется связаться с водителем.",
                order.getDriver().getFirstname(),
                order.getDriver().getLastName(),
                order.getDriver().getRatingAllTime().doubleValue(),
                order.getId(),
                order.getPlannedDeliveryTime()
        );

        dispatcherService.sendNotification(message);
    }
}
