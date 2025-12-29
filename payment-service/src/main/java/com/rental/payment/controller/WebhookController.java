package com.rental.payment.controller;

import com.rental.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    /** POST /api/webhooks/blockchain - Reçoit les webhooks de la blockchain */
    @PostMapping("/blockchain")
    public ResponseEntity<Map<String, Object>> handleBlockchainWebhook(
            @RequestBody Map<String, Object> payload) {

        log.info("🔗 Webhook blockchain reçu: {}", payload);

        try {
            String eventType = (String) payload.get("eventType");
            String txHash = (String) payload.get("transactionHash");
            Boolean confirmed = (Boolean) payload.get("confirmed");

            if ("TRANSACTION_CONFIRMED".equals(eventType) && txHash != null && confirmed != null) {
                if (confirmed) {
                    // Confirmer la transaction
                    paymentService.confirmTransaction(txHash);
                    log.info("✅ Transaction {} confirmée via webhook", txHash);
                } else {
                    log.warn("❌ Transaction {} échouée via webhook", txHash);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "status", "PROCESSED",
                    "message", "Webhook traité avec succès",
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement du webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    /** POST /api/webhooks/booking - Reçoit les webhooks du Booking Service */
    @PostMapping("/booking")
    public ResponseEntity<Map<String, Object>> handleBookingWebhook(
            @RequestBody Map<String, Object> payload) {

        log.info("📅 Webhook booking reçu: {}", payload);

        try {
            String eventType = (String) payload.get("eventType");
            Integer reservationId = (Integer) payload.get("reservationId");

            if ("BOOKING_CANCELLED".equals(eventType) && reservationId != null) {
                // Initier un remboursement
                paymentService.processRefund(reservationId, null, "Booking cancelled");
                log.info("💸 Remboursement initié pour réservation #{}", reservationId);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "PROCESSED",
                    "message", "Webhook booking traité",
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("❌ Erreur webhook booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    /** GET /api/webhooks/verify - Vérification d'un webhook (pour les services externes) */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam String challenge) {

        log.info("🔐 Vérification de webhook avec challenge: {}", challenge);

        // Retourner le challenge pour valider le webhook
        return ResponseEntity.ok(challenge);
    }
}