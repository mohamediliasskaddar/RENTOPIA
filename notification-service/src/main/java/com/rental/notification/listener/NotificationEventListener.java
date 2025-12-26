package com.rental.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.notification.config.RabbitMQConfig;
import com.rental.notification.dto.EmailRequest;
import com.rental.notification.dto.NotificationEvent;
import com.rental.notification.dto.NotificationRequest;
import com.rental.notification.enums.NotificationType;
import com.rental.notification.service.EmailService;
import com.rental.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.rental.notification.enums.NotificationType;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    /**
     * Écouter TOUS les événements de notification depuis RabbitMQ
     * Supporte: User, Booking, Payment, Review Services
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(String message) {
        try {
            log.info("📨 Received notification event: {}", message);

            // Convertir le message
            Map<String, Object> event = objectMapper.readValue(message, Map.class);

            // Extraire les données communes
            String typeStr = (String) event.get("type");
            String email = (String) event.get("email");
            String title = (String) event.get("title");
            String messageText = (String) event.get("message");
            Integer userId = (Integer) event.get("userId");
            Integer reservationId = (Integer) event.get("reservationId");

            // Identifier la source
            String source = determineSource(typeStr);

            // Traiter selon la source
            switch (source) {
                case "USER_SERVICE":
                    handleUserServiceEvent(event);
                    break;
                case "BOOKING_SERVICE":
                    handleBookingServiceEvent(event, userId, reservationId, typeStr);
                    break;
                case "PAYMENT_SERVICE":
                    handlePaymentServiceEvent(event, userId, reservationId, typeStr);
                    break;
                case "REVIEW_SERVICE":
                    handleReviewServiceEvent(event, userId, reservationId);
                    break;
                default:
                    log.warn("⚠️ Unknown event source: {}", event);
            }

            log.info("✅ Notification processed from {}", source);

        } catch (Exception e) {
            log.error("❌ Error processing notification event: {}", e.getMessage(), e);
        }
    }

    private String determineSource(String type) {
        if ("EMAIL_VERIFICATION".equals(type)) {
            return "USER_SERVICE";
        } else if (type != null && (
                type.contains("BOOKING") ||
                        type.contains("CHECK_IN") ||
                        type.contains("CHECK_OUT"))) {
            return "BOOKING_SERVICE";
        } else if (type != null && type.contains("PAYMENT")) {
            return "PAYMENT_SERVICE";
        } else if (type != null && type.contains("REVIEW")) {
            return "REVIEW_SERVICE";
        }
        return "UNKNOWN";
    }

    private void handleUserServiceEvent(Map<String, Object> event) {
        String email = (String) event.get("email");
        String token = (String) event.get("verificationToken");

        if (email != null && token != null) {
            emailService.sendVerificationEmail(email, token);
        }
    }

    private void handleBookingServiceEvent(Map<String, Object> event,
                                           Integer userId, Integer reservationId,
                                           String typeStr) {
        try {
            NotificationType type = NotificationType.valueOf(typeStr);
            String email = (String) event.get("email");
            String title = (String) event.get("title");
            String messageText = (String) event.get("message");

            // 1. Stocker en base
            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .reservationId(reservationId)
                    .notificationType(type)
                    .title(title != null ? title : getDefaultTitle(type))
                    .message(messageText != null ? messageText : getDefaultMessage(type))
                    .recipientEmail(email)
                    .sendEmail(email != null)
                    .build();

            notificationService.createNotification(request);

            // 2. Envoyer email
            if (email != null) {
                emailService.sendNotificationEmail(email, request.getTitle(),
                        request.getMessage(), type, event);
            }

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid notification type from Booking Service: {}", typeStr);
        }
    }

    private void handlePaymentServiceEvent(Map<String, Object> event,
                                           Integer userId, Integer reservationId,
                                           String typeStr) {
        try {
            NotificationType type = NotificationType.valueOf(typeStr);
            String email = (String) event.get("email");
            String amount = (String) event.get("amount");
            String currency = (String) event.get("currency");

            String title = "Paiement " + (type == NotificationType.PAYMENT_RECEIVED ? "réussi" : "échoué");
            String message = String.format("Votre paiement de %s %s a été %s",
                    amount, currency,
                    type == NotificationType.PAYMENT_RECEIVED ? "traité avec succès" : "refusé");

            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .reservationId(reservationId)
                    .notificationType(type)
                    .title(title)
                    .message(message)
                    .recipientEmail(email)
                    .sendEmail(email != null)
                    .build();

            notificationService.createNotification(request);

            if (email != null) {
                emailService.sendNotificationEmail(email, title, message, type, event);
            }

        } catch (Exception e) {
            log.error("❌ Error processing payment event: {}", e.getMessage());
        }
    }

    private void handleReviewServiceEvent(Map<String, Object> event,
                                          Integer userId, Integer reservationId) {
        try {
            String email = (String) event.get("email");
            String propertyName = (String) event.get("propertyName");

            String title = "Donnez votre avis sur " + (propertyName != null ? propertyName : "votre séjour");
            String message = "Comment s'est passé votre séjour ? Partagez votre expérience avec la communauté.";

            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .reservationId(reservationId)
                    .notificationType(NotificationType.REVIEW_REQUEST)
                    .title(title)
                    .message(message)
                    .recipientEmail(email)
                    .sendEmail(email != null)
                    .build();

            notificationService.createNotification(request);

            if (email != null) {
                emailService.sendNotificationEmail(email, title, message,
                        NotificationType.REVIEW_REQUEST, event);
            }

        } catch (Exception e) {
            log.error("❌ Error processing review event: {}", e.getMessage());
        }
    }

    private String getDefaultTitle(NotificationType type) {
        switch (type) {
            case BOOKING_CONFIRMATION: return "🎉 Réservation confirmée !";
            case BOOKING_CANCELLED: return "❌ Réservation annulée";
            case BOOKING_REQUEST_RECEIVED: return "📥 Nouvelle demande de réservation";
            case BOOKING_REQUEST_ACCEPTED: return "✅ Demande de réservation acceptée";
            case BOOKING_REQUEST_DECLINED: return "❌ Demande de réservation refusée";
            case CHECK_IN_REMINDER: return "⏰ Rappel : Check-in demain";
            case CHECK_OUT_REMINDER: return "⏰ Rappel : Check-out demain";
            default: return "Notification Rentopia";
        }
    }

    private String getDefaultMessage(NotificationType type) {
        switch (type) {
            case BOOKING_CONFIRMATION: return "Votre réservation a été confirmée avec succès.";
            case BOOKING_CANCELLED: return "Votre réservation a été annulée.";
            default: return "Vous avez reçu une notification de Rentopia.";
        }
    }
}
