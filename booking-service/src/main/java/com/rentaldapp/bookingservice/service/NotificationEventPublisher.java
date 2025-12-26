package com.rentaldapp.bookingservice.service;

import com.rentaldapp.bookingservice.config.NotificationRabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ NOUVEAU : Publisher RabbitMQ pour les notifications ASYNCHRONES
 * Remplace NotificationServiceClient (Feign synchrone)
 */
@Service
public class NotificationEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Envoyer une notification de confirmation de réservation
     */
    public void sendBookingConfirmation(Integer userId, Integer reservationId, String email) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "BOOKING_CONFIRMED");
            notification.put("userId", userId);
            notification.put("reservationId", reservationId);
            notification.put("email", email);
            notification.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    NotificationRabbitMQConfig.NOTIFICATION_EXCHANGE,
                    NotificationRabbitMQConfig.BOOKING_CONFIRMED_NOTIFICATION_KEY,
                    notification
            );

            logger.info("✅ Notification BOOKING_CONFIRMED envoyée pour reservation {}", reservationId);
        } catch (Exception e) {
            logger.error("❌ Échec d'envoi de notification BOOKING_CONFIRMED", e);
        }
    }

    /**
     * Envoyer une notification d'annulation
     */
    public void sendBookingCancellation(Integer userId, Integer reservationId, String email, String reason) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "BOOKING_CANCELLED");
            notification.put("userId", userId);
            notification.put("reservationId", reservationId);
            notification.put("email", email);
            notification.put("reason", reason);
            notification.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    NotificationRabbitMQConfig.NOTIFICATION_EXCHANGE,
                    NotificationRabbitMQConfig.BOOKING_CANCELLED_NOTIFICATION_KEY,
                    notification
            );

            logger.info("✅ Notification BOOKING_CANCELLED envoyée pour reservation {}", reservationId);
        } catch (Exception e) {
            logger.error("❌ Échec d'envoi de notification BOOKING_CANCELLED", e);
        }
    }

    /**
     * Envoyer un rappel de check-in
     */
    public void sendCheckInReminder(Integer userId, Integer reservationId, String email) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "CHECKIN_REMINDER");
            notification.put("userId", userId);
            notification.put("reservationId", reservationId);
            notification.put("email", email);
            notification.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    NotificationRabbitMQConfig.NOTIFICATION_EXCHANGE,
                    NotificationRabbitMQConfig.CHECKIN_REMINDER_KEY,
                    notification
            );

            logger.info("✅ Notification CHECKIN_REMINDER envoyée pour reservation {}", reservationId);
        } catch (Exception e) {
            logger.error("❌ Échec d'envoi de notification CHECKIN_REMINDER", e);
        }
    }

    /**
     * Notification de check-in effectué
     */
    public void sendCheckInCompleted(Integer userId, Integer reservationId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "CHECKIN_COMPLETED");
            notification.put("userId", userId);
            notification.put("reservationId", reservationId);
            notification.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    NotificationRabbitMQConfig.NOTIFICATION_EXCHANGE,
                    NotificationRabbitMQConfig.CHECKIN_COMPLETED_KEY,
                    notification
            );

            logger.info("✅ Notification CHECKIN_COMPLETED envoyée pour reservation {}", reservationId);
        } catch (Exception e) {
            logger.error("❌ Échec d'envoi de notification CHECKIN_COMPLETED", e);
        }
    }

    /**
     * Notification de check-out effectué
     */
    public void sendCheckOutCompleted(Integer userId, Integer reservationId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "CHECKOUT_COMPLETED");
            notification.put("userId", userId);
            notification.put("reservationId", reservationId);
            notification.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    NotificationRabbitMQConfig.NOTIFICATION_EXCHANGE,
                    NotificationRabbitMQConfig.CHECKOUT_COMPLETED_KEY,
                    notification
            );

            logger.info("✅ Notification CHECKOUT_COMPLETED envoyée pour reservation {}", reservationId);
        } catch (Exception e) {
            logger.error("❌ Échec d'envoi de notification CHECKOUT_COMPLETED", e);
        }
    }
}