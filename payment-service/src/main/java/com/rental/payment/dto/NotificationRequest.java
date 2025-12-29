package com.rental.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationRequest {
    private Integer userId;
    private String notificationType;  // Doit correspondre à l'enum du service notification
    private String message;
    private Integer reservationId;
    private String title;
    private Map<String, Object> data;

    // Méthodes factory avec les BONS types de notification
    public static NotificationRequest paymentReceived(Integer userId, Double amount, String txHash, Integer reservationId) {
        return NotificationRequest.builder()
                .userId(userId)
                .notificationType("PAYMENT_RECEIVED")  // CORRECT - existe dans l'enum
                .message("Votre paiement de " + amount + " ETH a été reçu")
                .title("Paiement reçu")
                .reservationId(reservationId)
                .data(Map.of(
                        "amount", amount,
                        "transactionHash", txHash,
                        "status", "RECEIVED"
                ))
                .build();
    }

    public static NotificationRequest paymentFailed(Integer userId, Double amount, String reason, Integer reservationId) {
        return NotificationRequest.builder()
                .userId(userId)
                .notificationType("PAYMENT_FAILED")  // CORRECT - existe dans l'enum
                .message("Votre paiement de " + amount + " ETH a échoué: " + reason)
                .title("Échec du paiement")
                .reservationId(reservationId)
                .data(Map.of(
                        "amount", amount,
                        "reason", reason,
                        "status", "FAILED"
                ))
                .build();
    }

    public static NotificationRequest bookingConfirmation(Integer userId, String message, Integer reservationId) {
        return NotificationRequest.builder()
                .userId(userId)
                .notificationType("BOOKING_CONFIRMATION")  // CORRECT - existe dans l'enum
                .message(message)
                .title("Réservation confirmée")
                .reservationId(reservationId)
                .data(Map.of(
                        "status", "CONFIRMED"
                ))
                .build();
    }

    // Builder avec valeurs par défaut pour le titre
    public static class NotificationRequestBuilder {
        private String title = "Notification";
    }
}