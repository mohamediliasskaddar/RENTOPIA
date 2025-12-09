package com.rental.blockchain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour la communication inter-services
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queues.booking-created}")
    private String bookingCreatedQueue;

    @Value("${rabbitmq.queues.booking-confirmed}")
    private String bookingConfirmedQueue;

    @Value("${rabbitmq.queues.checkin-completed}")
    private String checkinCompletedQueue;

    @Value("${rabbitmq.queues.checkout-completed}")
    private String checkoutCompletedQueue;

    /**
     * Exchange principal
     */
    @Bean
    public TopicExchange exchange() {
        log.info("📡 Création de l'exchange: {}", exchange);
        return new TopicExchange(exchange);
    }

    /**
     * Queue: Réservation créée (depuis Booking Service)
     */
    @Bean
    public Queue bookingCreatedQueue() {
        return new Queue(bookingCreatedQueue, true);
    }

    /**
     * Queue: Réservation confirmée (vers Booking Service)
     */
    @Bean
    public Queue bookingConfirmedQueue() {
        return new Queue(bookingConfirmedQueue, true);
    }

    /**
     * Queue: Check-in complété
     */
    @Bean
    public Queue checkinCompletedQueue() {
        return new Queue(checkinCompletedQueue, true);
    }

    /**
     * Queue: Check-out complété
     */
    @Bean
    public Queue checkoutCompletedQueue() {
        return new Queue(checkoutCompletedQueue, true);
    }

    /**
     * Binding: booking.created -> bookingCreatedQueue
     */
    @Bean
    public Binding bookingCreatedBinding() {
        return BindingBuilder
                .bind(bookingCreatedQueue())
                .to(exchange())
                .with("booking.created");
    }

    /**
     * Binding: booking.confirmed -> bookingConfirmedQueue
     */
    @Bean
    public Binding bookingConfirmedBinding() {
        return BindingBuilder
                .bind(bookingConfirmedQueue())
                .to(exchange())
                .with("booking.confirmed");
    }

    /**
     * Binding: checkin.completed -> checkinCompletedQueue
     */
    @Bean
    public Binding checkinCompletedBinding() {
        return BindingBuilder
                .bind(checkinCompletedQueue())
                .to(exchange())
                .with("checkin.completed");
    }

    /**
     * Binding: checkout.completed -> checkoutCompletedQueue
     */
    @Bean
    public Binding checkoutCompletedBinding() {
        return BindingBuilder
                .bind(checkoutCompletedQueue())
                .to(exchange())
                .with("checkout.completed");
    }

    /**
     * Converter JSON pour RabbitMQ
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configuré avec le converter JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}