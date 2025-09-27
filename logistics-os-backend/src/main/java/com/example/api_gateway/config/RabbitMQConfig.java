package com.example.api_gateway.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Очереди
    @Bean
    public Queue newOrderQueue() {
        return new Queue("new.order.queue", true);
    }

    @Bean
    public Queue orderCompletedQueue() {
        return new Queue("order.completed.queue", true);
    }

    @Bean
    public Queue driverAssignedQueue() {
        return new Queue("driver.assigned.queue", true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue("notification.queue", true);
    }

    // Конвертер JSON
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
