package com.rentaldapp.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Noms des échanges
    public static final String BOOKING_EXCHANGE = "booking.exchange";

    // Routing keys
    public static final String BOOKING_CREATED_KEY = "booking.created";
    public static final String BOOKING_CONFIRMED_KEY = "booking.confirmed";
    public static final String BOOKING_CANCELLED_KEY = "booking.cancelled";
    public static final String BOOKING_COMPLETED_KEY = "booking.completed";

    // Noms des files d'attente
    public static final String BOOKING_CREATED_QUEUE = "booking.created.queue";
    public static final String BOOKING_CONFIRMED_QUEUE = "booking.confirmed.queue";
    public static final String BOOKING_CANCELLED_QUEUE = "booking.cancelled.queue";
    public static final String BOOKING_COMPLETED_QUEUE = "booking.completed.queue";

    /**
     * Échange topic pour les événements de réservation
     */
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    /**
     * File d'attente pour les événements de création
     */
    @Bean
    public Queue bookingCreatedQueue() {
        return new Queue(BOOKING_CREATED_QUEUE, true);
    }

    @Bean
    public Binding bookingCreatedBinding() {
        return BindingBuilder.bind(bookingCreatedQueue())
                .to(bookingExchange())
                .with(BOOKING_CREATED_KEY);
    }

    /**
     * File d'attente pour les événements de confirmation
     */
    @Bean
    public Queue bookingConfirmedQueue() {
        return new Queue(BOOKING_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Binding bookingConfirmedBinding() {
        return BindingBuilder.bind(bookingConfirmedQueue())
                .to(bookingExchange())
                .with(BOOKING_CONFIRMED_KEY);
    }

    /**
     * File d'attente pour les événements d'annulation
     */
    @Bean
    public Queue bookingCancelledQueue() {
        return new Queue(BOOKING_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding bookingCancelledBinding() {
        return BindingBuilder.bind(bookingCancelledQueue())
                .to(bookingExchange())
                .with(BOOKING_CANCELLED_KEY);
    }

    /**
     * File d'attente pour les événements de fin
     */
    @Bean
    public Queue bookingCompletedQueue() {
        return new Queue(BOOKING_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding bookingCompletedBinding() {
        return BindingBuilder.bind(bookingCompletedQueue())
                .to(bookingExchange())
                .with(BOOKING_COMPLETED_KEY);
    }
}