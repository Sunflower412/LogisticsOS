package com.example.api_gateway.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @RabbitListener(queues = "new.order.queue")
    public void handleNewOrder(Long orderId) {
        System.out.println("Received new order: " + orderId);
        // Здесь можно добавить логику уведомлений
    }

    @RabbitListener(queues = "order.completed.queue")
    public void handleOrderCompleted(String message) {
        System.out.println("Order completed: " + message);
    }
}
