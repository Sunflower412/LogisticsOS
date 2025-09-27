package com.example.api_gateway.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNewOrderEvent(Long orderId) {
        rabbitTemplate.convertAndSend("new.order.queue", orderId);
    }

    public void sendOrderCompletedEvent(Long orderId, Long driverId, boolean success) {
        String message = String.format("Order %d completed by driver %d. Success: %s",
                orderId, driverId, success);
        rabbitTemplate.convertAndSend("order.completed.queue", message);
    }

    public void sendDriverAssignedEvent(Long orderId, Long driverId) {
        String message = String.format("Driver %d assigned to order %d", driverId, orderId);
        rabbitTemplate.convertAndSend("driver.assigned.queue", message);
    }

    public void sendNotification(String message) {
        rabbitTemplate.convertAndSend("notification.queue", message);
    }
}
