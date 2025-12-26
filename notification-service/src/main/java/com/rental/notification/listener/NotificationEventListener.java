package com.rental.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.notification.dto.NotificationRequest;
import com.rental.notification.enums.NotificationType;
import com.rental.notification.service.EmailService;
import com.rental.notification.service.NotificationService;
import com.rental.notification.util.JavaDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final JavaDeserializer javaDeserializer; // AJOUTER CE CHAMP

    /**
     * Écouter TOUS les événements de notification depuis RabbitMQ
     * Modifier pour recevoir Message au lieu de Map
     */
    @RabbitListener(queues = "${rabbitmq.queue.notification:notification.queue}")
    public void handleNotificationEvent(Message message) { // Changer de Map<String, Object> à Message
        try {
            byte[] body = message.getBody();
            log.info("📨 Message RabbitMQ reçu ({} bytes)", body.length);

            // 1. D'abord, essayer de désérialiser avec JavaDeserializer
            Map<String, Object> event = javaDeserializer.deserializeEmailVerificationEvent(body);
            log.info("✅ Événement désérialisé: {}", event);

            // 2. Extraire les données
            String type = (String) event.get("type");
            String email = (String) event.get("email");
            String token = (String) event.get("verificationToken");

            if (token == null) {
                token = (String) event.get("token");
            }

            log.info("📋 Données extraites - Type: {}, Email: {}, Token: {}",
                    type, email, token != null ? "***" + token.substring(token.length() - 4) : "null");

            // 3. Déterminer la source et traiter
            String source = determineSource(type);
            log.info("🏷️ Source détectée: {}", source);

            switch (source) {
                case "USER_SERVICE":
                    handleUserServiceEvent(event);
                    break;

                case "BOOKING_SERVICE":
                    handleBookingServiceEvent(event);
                    break;

                case "PAYMENT_SERVICE":
                    handlePaymentServiceEvent(event);
                    break;

                case "REVIEW_SERVICE":
                    handleReviewServiceEvent(event);
                    break;


                default:
                    log.warn("⚠️ Source inconnue: {}", source);
                    // Essayer de traiter comme email de vérification de toute façon
                    if ("EMAIL_VERIFICATION".equals(type) && email != null && token != null) {
                        log.info("🔐 Tentative d'envoi email de vérification...");
                        handleUserServiceEvent(event);
                    }
                    break;
            }

        } catch (Exception e) {
            log.error("❌ Erreur dans NotificationEventListener: {}", e.getMessage(), e);
        }
    }

    private String determineSource(String type) {
        if (type == null) {
            return "UNKNOWN";
        }

        if ("EMAIL_VERIFICATION".equals(type)) {
            return "USER_SERVICE";
        } else if (type.contains("BOOKING") ||
                type.contains("CHECK_IN") ||
                type.contains("CHECK_OUT")) {
            return "BOOKING_SERVICE";
        } else if (type.contains("PAYMENT")) {
            return "PAYMENT_SERVICE";
        } else if (type.contains("REVIEW")) {
            return "REVIEW_SERVICE";
        }
        return "UNKNOWN";
    }

    private void handleUserServiceEvent(Map<String, Object> event) {
        String email = (String) event.get("email");
        String token = (String) event.get("verificationToken");

        if (token == null) {
            token = (String) event.get("token");
        }

        if (email != null && token != null) {
            log.info("🔐 Envoi email de vérification à: {} avec token: {}...",
                    email, token.substring(0, Math.min(8, token.length())) + "...");

            try {
                emailService.sendVerificationEmail(email, token);
                log.info("✅ Email de vérification envoyé à: {}", email);

                // Optionnel: Stocker la notification en base
                /*
                NotificationRequest request = NotificationRequest.builder()
                        .userId(extractUserId(event)) // Vous devez extraire userId de l'événement
                        .notificationType(NotificationType.EMAIL_VERIFICATION)
                        .title("Email de vérification envoyé")
                        .message("Un email de vérification a été envoyé à votre adresse")
                        .recipientEmail(email)
                        .sendEmail(false) // Déjà envoyé
                        .build();

                notificationService.createNotification(request);
                */

            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email: {}", e.getMessage());
            }
        } else {
            log.error("❌ Données manquantes pour USER_SERVICE: email={}, token={}", email, token);
            log.error("   Événement complet: {}", event);
        }
    }

    private void handleBookingServiceEvent(Map<String, Object> event) {
        try {
            String typeStr = (String) event.get("type");
            NotificationType type = NotificationType.valueOf(typeStr);
            String email = (String) event.get("email");
            String title = (String) event.get("title");
            String messageText = (String) event.get("message");
            Integer userId = (Integer) event.get("userId");
            Integer reservationId = (Integer) event.get("reservationId");

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
                log.info("✅ Email de réservation envoyé à: {}", email);
            }

        } catch (IllegalArgumentException e) {
            log.error("❌ Type de notification invalide de Booking Service: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de l'événement booking: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentServiceEvent(Map<String, Object> event) {
        try {
            String typeStr = (String) event.get("type");
            NotificationType type = NotificationType.valueOf(typeStr);
            String email = (String) event.get("email");
            String amount = (String) event.get("amount");
            String currency = (String) event.get("currency");
            Integer userId = (Integer) event.get("userId");
            Integer reservationId = (Integer) event.get("reservationId");

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
                log.info("✅ Email de paiement envoyé à: {}", email);
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de l'événement payment: {}", e.getMessage(), e);
        }
    }

    private void handleReviewServiceEvent(Map<String, Object> event) {
        try {
            String email = (String) event.get("email");
            String propertyName = (String) event.get("propertyName");
            Integer userId = (Integer) event.get("userId");
            Integer reservationId = (Integer) event.get("reservationId");

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
                log.info("✅ Email de review envoyé à: {}", email);
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de l'événement review: {}", e.getMessage(), e);
        }
    }

    private Integer extractUserId(Map<String, Object> event) {
        // Méthode pour extraire userId de l'événement
        // À adapter selon votre structure d'événement
        Object userIdObj = event.get("userId");
        if (userIdObj instanceof Integer) {
            return (Integer) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Integer.parseInt((String) userIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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