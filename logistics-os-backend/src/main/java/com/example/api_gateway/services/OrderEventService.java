package com.example.api_gateway.services;

import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderEventService {
    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final RabbitMQService rabbitMQService;

    public OrderEventService(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

    /**
     * Отправка события о создании нового заказа
     */
    @Async
    public void sendOrderCreatedEvent(Long orderId) {
        try {
            log.info("Sending order created event for order ID: {}", orderId);
            rabbitMQService.sendNewOrderEvent(orderId);
            log.debug("Order created event sent successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send order created event for order ID: {}", orderId, e);
            // Можно добавить логику повторной отправки или сохранения в БД для последующей отправки
        }
    }

    /**
     * Отправка события о назначении водителя на заказ
     */
    @Async
    public void sendDriverAssignedEvent(Long orderId, Long driverId) {
        try {
            log.info("Sending driver assigned event - Order ID: {}, Driver ID: {}", orderId, driverId);
            rabbitMQService.sendDriverAssignedEvent(orderId, driverId);
            log.debug("Driver assigned event sent successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send driver assigned event for order ID: {} and driver ID: {}",
                    orderId, driverId, e);
        }
    }

    /**
     * Отправка события об изменении статуса заказа
     */
    @Async
    public void sendOrderStatusChangedEvent(Long orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        try {
            log.info("Sending order status changed event - Order ID: {}, Status: {} → {}",
                    orderId, oldStatus, newStatus);

            rabbitMQService.sendNotification(
                    String.format("Статус заказа #%d изменен: %s → %s", orderId, oldStatus, newStatus)
            );

            log.debug("Order status changed event sent successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send order status changed event for order ID: {}", orderId, e);
        }
    }

    /**
     * Отправка события о завершении заказа
     */
    @Async
    public void sendOrderCompletedEvent(Long orderId, Long driverId, boolean success) {
        try {
            log.info("Sending order completed event - Order ID: {}, Driver ID: {}, Success: {}",
                    orderId, driverId, success);

            rabbitMQService.sendOrderCompletedEvent(orderId, driverId, success);
            log.debug("Order completed event sent successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send order completed event for order ID: {} and driver ID: {}",
                    orderId, driverId, e);
        }
    }

    /**
     * Отправка уведомления диспетчеру
     */
    @Async
    public void sendDispatcherNotification(String message) {
        try {
            log.info("Sending dispatcher notification: {}", message);
            rabbitMQService.sendNotification(message);
            log.debug("Dispatcher notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send dispatcher notification: {}", message, e);
        }
    }

    /**
     * Отправка события о провале заказа
     */
    @Async
    public void sendOrderFailedEvent(Long orderId, String reason, Long driverId) {
        try {
            log.info("Sending order failed event - Order ID: {}, Reason: {}, Driver ID: {}",
                    orderId, reason, driverId);

            // Отправляем уведомление о провале
            rabbitMQService.sendNotification(
                    String.format("Заказ #%d провален. Причина: %s", orderId, reason)
            );

            // Отправляем событие о завершении (неуспешном)
            if (driverId != null) {
                rabbitMQService.sendOrderCompletedEvent(orderId, driverId, false);
            }

            log.debug("Order failed event sent successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send order failed event for order ID: {}", orderId, e);
        }
    }

    /**
     * Отправка события о ручном назначении водителя
     */
    @Async
    public void sendManualDriverAssignmentEvent(Long orderId, Long driverId,
                                                String driverName, double driverRating) {
        try {
            log.info("Sending manual driver assignment event - Order ID: {}, Driver ID: {}",
                    orderId, driverId);

            rabbitMQService.sendDriverAssignedEvent(orderId, driverId);

            rabbitMQService.sendNotification(
                    String.format("✅ Водитель %s (рейтинг: %.2f) назначен на заказ #%d",
                            driverName, driverRating, orderId)
            );

            log.debug("Manual driver assignment event sent successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send manual driver assignment event for order ID: {} and driver ID: {}",
                    orderId, driverId, e);
        }
    }

    /**
     * Отправка ошибки назначения водителя
     */
    @Async
    public void sendDriverAssignmentError(String errorMessage) {
        try {
            log.warn("Sending driver assignment error: {}", errorMessage);
            rabbitMQService.sendNotification("❌ Ошибка назначения водителя: " + errorMessage);
        } catch (Exception e) {
            log.error("Failed to send driver assignment error notification", e);
        }
    }

    /**
     * Комплексное событие о завершении заказа с уведомлением диспетчеру
     */
    @Async
    public void sendOrderCompletionWithDispatcherNotification(Long orderId, Long driverId,
                                                              boolean success, String driverName) {
        try {
            log.info("Sending comprehensive order completion event - Order ID: {}, Success: {}",
                    orderId, success);

            // Основное событие о завершении
            rabbitMQService.sendOrderCompletedEvent(orderId, driverId, success);

            // Уведомление диспетчеру
            String resultMessage = success ? "успешно завершен" : "завершен с ошибкой";
            String notification = String.format("Заказ #%d %s. Водитель: %s",
                    orderId, resultMessage, driverName);

            rabbitMQService.sendNotification(notification);

            log.debug("Comprehensive order completion event sent successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to send comprehensive order completion event for order ID: {}", orderId, e);
        }
    }
}