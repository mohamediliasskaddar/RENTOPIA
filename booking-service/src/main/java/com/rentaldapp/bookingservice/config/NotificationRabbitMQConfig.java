package com.rentaldapp.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour les notifications ASYNCHRONES
 * ✅ NOUVEAU : Remplace les appels Feign synchrones par des messages RabbitMQ
 */
@Configuration
public class NotificationRabbitMQConfig {

    // Nom de l'échange pour les notifications
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Routing keys
    public static final String BOOKING_CONFIRMED_NOTIFICATION_KEY = "notification.booking.confirmed";
    public static final String BOOKING_CANCELLED_NOTIFICATION_KEY = "notification.booking.cancelled";
    public static final String CHECKIN_REMINDER_KEY = "notification.checkin.reminder";
    public static final String CHECKIN_COMPLETED_KEY = "notification.checkin.completed";
    public static final String CHECKOUT_COMPLETED_KEY = "notification.checkout.completed";

    // Noms des queues (seront consommées par le Notification Service)
    public static final String BOOKING_CONFIRMED_NOTIFICATION_QUEUE = "notification.booking.confirmed.queue";
    public static final String BOOKING_CANCELLED_NOTIFICATION_QUEUE = "notification.booking.cancelled.queue";
    public static final String CHECKIN_REMINDER_QUEUE = "notification.checkin.reminder.queue";
    public static final String CHECKIN_COMPLETED_QUEUE = "notification.checkin.completed.queue";
    public static final String CHECKOUT_COMPLETED_QUEUE = "notification.checkout.completed.queue";

    /**
     * Échange topic pour les notifications
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    // ========== BOOKING CONFIRMED ==========
    @Bean
    public Queue bookingConfirmedNotificationQueue() {
        return new Queue(BOOKING_CONFIRMED_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding bookingConfirmedNotificationBinding() {
        return BindingBuilder
                .bind(bookingConfirmedNotificationQueue())
                .to(notificationExchange())
                .with(BOOKING_CONFIRMED_NOTIFICATION_KEY);
    }

    // ========== BOOKING CANCELLED ==========
    @Bean
    public Queue bookingCancelledNotificationQueue() {
        return new Queue(BOOKING_CANCELLED_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding bookingCancelledNotificationBinding() {
        return BindingBuilder
                .bind(bookingCancelledNotificationQueue())
                .to(notificationExchange())
                .with(BOOKING_CANCELLED_NOTIFICATION_KEY);
    }

    // ========== CHECK-IN REMINDER ==========
    @Bean
    public Queue checkinReminderQueue() {
        return new Queue(CHECKIN_REMINDER_QUEUE, true);
    }

    @Bean
    public Binding checkinReminderBinding() {
        return BindingBuilder
                .bind(checkinReminderQueue())
                .to(notificationExchange())
                .with(CHECKIN_REMINDER_KEY);
    }

    // ========== CHECK-IN COMPLETED ==========
    @Bean
    public Queue checkinCompletedQueue() {
        return new Queue(CHECKIN_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding checkinCompletedBinding() {
        return BindingBuilder
                .bind(checkinCompletedQueue())
                .to(notificationExchange())
                .with(CHECKIN_COMPLETED_KEY);
    }

    // ========== CHECK-OUT COMPLETED ==========
    @Bean
    public Queue checkoutCompletedQueue() {
        return new Queue(CHECKOUT_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding checkoutCompletedBinding() {
        return BindingBuilder
                .bind(checkoutCompletedQueue())
                .to(notificationExchange())
                .with(CHECKOUT_COMPLETED_KEY);
    }
}