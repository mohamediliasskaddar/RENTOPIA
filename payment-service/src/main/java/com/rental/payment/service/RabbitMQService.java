package com.rental.payment.service;

import com.rental.payment.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service

@Slf4j
public class RabbitMQService {  // ENLEVEZ @RequiredArgsConstructor

    private final RabbitTemplate rabbitTemplate;
    private final PaymentService paymentService;

    // CONSTRUCTEUR PERSONNALISÉ avec @Lazy
    @Autowired
    public RabbitMQService(
            RabbitTemplate rabbitTemplate,
            @Lazy PaymentService paymentService) {

        this.rabbitTemplate = rabbitTemplate;
        this.paymentService = paymentService;
    }

    // ========== LISTENERS ENTRANTS ==========

    /**
     * 📥 Écouter les COMMANDES depuis Booking Service
     * Queue: payment.queue
     */
    @RabbitListener(queues = "payment.queue")
    public void handleBookingCommands(PaymentMessage message) {
        log.info("📨 📥 Commande reçue de Booking Service");
        log.info("   Type: {}", message.getType());
        log.info("   Réservation: #{}", message.getReservationId());

        try {
            switch (message.getType()) {
                case INITIATE_PAYMENT:
                    log.info("💰 Commande: Initier paiement");
                    log.info("   Cette opération se fait via REST API /api/payments/create");
                    log.info("   Le frontend appelle directement le endpoint avec MetaMask");
                    break;

                case RELEASE_ESCROW:
                    log.info("🔓 Commande: Libérer escrow pour réservation #{}",
                            message.getReservationId());

                    try {
                        PaymentResponse response = paymentService.releaseEscrow(
                                message.getReservationId()
                        );
                        log.info("✅ Escrow libéré avec succès: {}", response.getTransactionHash());

                        // Notifier le succès
                        publishEscrowReleased(
                                message.getReservationId(),
                                response.getTransactionHash()
                        );

                    } catch (Exception e) {
                        log.error("❌ Échec libération escrow: {}", e.getMessage());

                        // Notifier l'échec
                        sendToBookingService("ESCROW_RELEASE_FAILED", Map.of(
                                "reservationId", message.getReservationId(),
                                "error", e.getMessage(),
                                "status", "FAILED"
                        ));
                    }
                    break;

                case REFUND:
                    log.info("💸 Commande: Rembourser réservation #{}",
                            message.getReservationId());

                    try {
                        PaymentResponse response = paymentService.processRefund(
                                message.getReservationId(),
                                message.getAmount(),
                                "Remboursement demandé par Booking Service"
                        );
                        log.info("✅ Remboursement traité: {}", response.getTransactionHash());

                        // Notifier le succès
                        publishRefundProcessed(
                                message.getReservationId(),
                                response.getTransactionHash(),
                                message.getAmount()
                        );

                    } catch (Exception e) {
                        log.error("❌ Échec remboursement: {}", e.getMessage());

                        // Notifier l'échec
                        sendToBookingService("REFUND_FAILED", Map.of(
                                "reservationId", message.getReservationId(),
                                "error", e.getMessage(),
                                "status", "FAILED"
                        ));
                    }
                    break;

                case PAYMENT_CONFIRMED:
                    log.info("✅ Notification: Paiement confirmé pour réservation #{}",
                            message.getReservationId());
                    // Ce cas est géré par le webhook ou le polling
                    break;

                case PAYMENT_FAILED:
                    log.warn("❌ Notification: Paiement échoué pour réservation #{}",
                            message.getReservationId());
                    // Logique de gestion d'échec si nécessaire
                    break;

                default:
                    log.warn("⚠️ Type de commande non reconnu: {}", message.getType());
            }
        } catch (Exception e) {
            log.error("❌ Erreur traitement commande Booking: {}", e.getMessage(), e);
        }
    }

    /**
     * 📥 Écouter les ÉVÉNEMENTS depuis Blockchain Service
     * Queue: blockchain.queue
     *
     * Note: Le Blockchain Service publie des événements quand une transaction
     * est confirmée on-chain (par exemple après X blocs de confirmation)
     */
    @RabbitListener(queues = "blockchain.queue")
    public void handleBlockchainEvents(Map<String, Object> event) {
        log.info("🔗 📥 Événement blockchain reçu");
        log.info("   Event: {}", event);

        try {
            String eventType = (String) event.get("eventType");

            if ("TRANSACTION_CONFIRMED".equals(eventType)) {
                String txHash = (String) event.get("transactionHash");
                Boolean confirmed = (Boolean) event.get("confirmed");

                log.info("🔗 Transaction: {}", txHash);
                log.info("   Confirmée: {}", confirmed);

                if (Boolean.TRUE.equals(confirmed)) {
                    log.info("✅ Transaction confirmée on-chain: {}", txHash);

                    try {
                        PaymentResponse response = paymentService.confirmTransaction(txHash);
                        log.info("✅ Paiement mis à jour en base: Transaction #{}",
                                response.getTransactionId());

                        // Publier événement de confirmation
                        sendToBookingService("PAYMENT_CONFIRMED_ON_CHAIN", Map.of(
                                "transactionHash", txHash,
                                "transactionId", response.getTransactionId(),
                                "reservationId", response.getTransactionId(), // À adapter
                                "status", "CONFIRMED"
                        ));

                    } catch (Exception e) {
                        log.error("❌ Erreur mise à jour paiement: {}", e.getMessage());
                    }
                } else {
                    log.warn("❌ Transaction échouée on-chain: {}", txHash);

                    // Gérer l'échec
                    sendToBookingService("PAYMENT_FAILED_ON_CHAIN", Map.of(
                            "transactionHash", txHash,
                            "status", "FAILED"
                    ));
                }
            } else if ("ESCROW_RELEASED".equals(eventType)) {
                log.info("🔓 Événement: Escrow libéré on-chain");
                String txHash = (String) event.get("transactionHash");
                Integer reservationId = (Integer) event.get("reservationId");

                // Mettre à jour le statut si nécessaire
                log.info("✅ Escrow libéré: TX {}, Réservation #{}", txHash, reservationId);
            }

        } catch (Exception e) {
            log.error("❌ Erreur traitement événement blockchain: {}", e.getMessage(), e);
        }
    }

    // ========== MÉTHODES D'ENVOI ==========

    /**
     * 📤 Envoyer un message générique vers Booking Service
     */
    public void sendToBookingService(String eventType, Object payload) {
        try {
            rabbitTemplate.convertAndSend(
                    "rental.exchange",
                    "booking." + eventType.toLowerCase(),
                    payload
            );
            log.info("📤 ✅ Message envoyé à Booking Service: {}", eventType);
        } catch (Exception e) {
            log.error("📤 ❌ Erreur envoi à Booking Service: {}", e.getMessage());
        }
    }

    /**
     * 📤 Envoyer un message générique vers Notification Service
     */
    public void sendToNotificationService(String eventType, Object payload) {
        try {
            rabbitTemplate.convertAndSend(
                    "rental.exchange",
                    "notification." + eventType.toLowerCase(),
                    payload
            );
            log.info("📤 ✅ Message envoyé à Notification Service: {}", eventType);
        } catch (Exception e) {
            log.error("📤 ❌ Erreur envoi à Notification Service: {}", e.getMessage());
        }
    }

    /**
     * 📤 Publier événement: Paiement confirmé
     */
    public void publishPaymentConfirmed(Integer reservationId, String txHash, Double amount) {
        log.info("📤 Publication: Paiement confirmé");

        PaymentConfirmedEvent event = PaymentConfirmedEvent.builder()
                .reservationId(reservationId)
                .transactionHash(txHash)
                .amountEth(amount)
                .status("CONFIRMED")
                .build();

        sendToBookingService("PAYMENT_CONFIRMED", event);

        log.info("   Réservation: #{}", reservationId);
        log.info("   Transaction: {}", txHash);
        log.info("   Montant: {} ETH", amount);
    }

    /**
     * 📤 Publier événement: Escrow libéré
     */
    public void publishEscrowReleased(Integer reservationId, String txHash) {
        log.info("📤 Publication: Escrow libéré");

        sendToBookingService("ESCROW_RELEASED", Map.of(
                "reservationId", reservationId,
                "transactionHash", txHash,
                "status", "RELEASED",
                "timestamp", java.time.LocalDateTime.now()
        ));

        log.info("   Réservation: #{}", reservationId);
        log.info("   Transaction: {}", txHash);
    }

    /**
     * 📤 Publier événement: Remboursement traité
     */
    public void publishRefundProcessed(Integer reservationId, String txHash, Double amount) {
        log.info("📤 Publication: Remboursement traité");

        sendToBookingService("REFUND_PROCESSED", Map.of(
                "reservationId", reservationId,
                "transactionHash", txHash,
                "amount", amount,
                "status", "REFUNDED",
                "timestamp", java.time.LocalDateTime.now()
        ));

        log.info("   Réservation: #{}", reservationId);
        log.info("   Transaction: {}", txHash);
        log.info("   Montant: {} ETH", amount);
    }

    /**
     * 📤 Publier événement: Paiement échoué
     */
    public void publishPaymentFailed(Integer reservationId, String reason) {
        log.warn("📤 Publication: Paiement échoué");

        sendToBookingService("PAYMENT_FAILED", Map.of(
                "reservationId", reservationId,
                "reason", reason,
                "status", "FAILED",
                "timestamp", java.time.LocalDateTime.now()
        ));

        log.warn("   Réservation: #{}", reservationId);
        log.warn("   Raison: {}", reason);
    }
}