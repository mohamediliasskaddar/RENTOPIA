package com.rental.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.payment.client.BlockchainServiceClient;
import com.rental.payment.client.BookingServiceClient;
import com.rental.payment.client.NotificationServiceClient;
import com.rental.payment.dto.*;
import com.rental.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrateur pour gérer les appels aux services externes
 * avec retry automatique et circuit breaker
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalServiceOrchestrator {

    private final BlockchainServiceClient blockchainServiceClient;
    private final BookingServiceClient bookingServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // ✅ INJECTION DE LA CLÉ PRIVÉE ADMIN DEPUIS application.yml
    @Value("${blockchain.wallet.private-key}")
    private String adminPrivateKey;

    /**
     * Exécuter une transaction blockchain avec retry automatique
     *
     * @param request Requête de transaction blockchain
     * @return Réponse de la transaction
     * @throws PaymentException si la transaction échoue après tous les retries
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2),
            recover = "recoverBlockchainTransaction"
    )
    public BlockchainTransactionResponse executeBlockchainTransaction(
            CreateBookingBlockchainRequest request) {

        log.info("🔗 [Tentative] Exécution de transaction blockchain pour property #{}",
                request.getPropertyId());

        try {
            BlockchainTransactionResponse result = blockchainServiceClient.createBookingTransaction(request);

            if (result == null) {
                throw new RuntimeException("Réponse blockchain nulle");
            }

            log.info("✅ Transaction blockchain exécutée avec succès: {}",
                    result.getTransactionHash());

            return result;

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'exécution blockchain: {}", e.getMessage());
            throw new RuntimeException("Échec blockchain: " + e.getMessage(), e);
        }
    }

    /**
     * Méthode de récupération si tous les retries échouent
     */
    @Recover
    public BlockchainTransactionResponse recoverBlockchainTransaction(
            RuntimeException e,
            CreateBookingBlockchainRequest request) {

        log.error("🛑 ÉCHEC DÉFINITIF de la transaction blockchain après tous les retries");
        log.error("   Property: #{}", request.getPropertyId());
        log.error("   Erreur: {}", e.getMessage());

        // Retourner une réponse d'échec
        return BlockchainTransactionResponse.builder()
                .status("FAILED")
                .message("Service blockchain indisponible après plusieurs tentatives")
                .success(false)
                .build();
    }

    /**
     * Récupérer les détails d'une réservation
     *
     * @param reservationId ID de la réservation
     * @return Détails de la réservation ou null si échec
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public Map<String, Object> getBookingDetails(Integer reservationId) {
        try {
            log.info("📋 Récupération des détails de réservation #{}", reservationId);

            Map<String, Object> bookingResponse = bookingServiceClient.getBookingById(reservationId);

            if (bookingResponse != null && !bookingResponse.isEmpty()) {
                log.info("✅ Détails de réservation #{} récupérés", reservationId);
                return bookingResponse;
            } else {
                log.warn("⚠️ Échec récupération détails réservation - réponse vide ou nulle");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Erreur récupération détails réservation: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Confirmer une réservation après paiement
     *
     * @param reservationId ID de la réservation
     * @param txHash Hash de la transaction blockchain
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public void confirmBooking(Integer reservationId, String txHash) {
        log.info("✅ [Tentative] Confirmation de réservation #{} avec TX {}",
                reservationId, txHash);

        try {
            Map<String, Object> response = bookingServiceClient.confirmBooking(reservationId, txHash);

            if (response != null) {
                log.info("✅ Réservation #{} confirmée avec succès. Réponse: {}",
                        reservationId, response.get("message"));
            } else {
                log.warn("⚠️ Échec de la confirmation - réponse nulle");
            }

        } catch (Exception e) {
            log.error("❌ Erreur confirmation booking: {}", e.getMessage());
            // Ne pas propager l'erreur - la confirmation n'est pas critique
        }
    }

    /**
     * Libérer l'escrow d'une réservation via booking service
     *
     * @param reservationId ID de la réservation (Long)
     * @param txHash Hash de la transaction
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public void releaseBookingEscrow(Long reservationId, String txHash) {
        try {
            log.info("🔓 [Tentative] Libération d'escrow pour réservation #{}", reservationId);

            // CONVERTIR Long en Integer (attention aux valeurs > Integer.MAX_VALUE)
            if (reservationId > Integer.MAX_VALUE) {
                log.error("❌ ID de réservation trop grand: {}", reservationId);
                return;
            }

            Integer reservationIdInt = reservationId.intValue();

            Map<String, Object> response =
                    bookingServiceClient.releaseBookingEscrow(reservationIdInt, txHash);

            if (response != null) {
                log.info("✅ Escrow libéré pour réservation #{}. Réponse: {}",
                        reservationId, response.get("message"));
            } else {
                log.warn("⚠️ Échec de libération d'escrow - réponse nulle");
            }

        } catch (Exception e) {
            log.error("❌ Erreur libération escrow: {}", e.getMessage());
        }
    }

    /**
     * ✅ CORRIGÉ : Libérer l'escrow via blockchain service
     * Utilise la clé admin depuis la configuration (pas hardcodée)
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public BlockchainTransactionResponse releaseEscrowViaBlockchain(Long reservationId) {
        try {
            log.info("🔗 [Tentative] Libération d'escrow via blockchain pour réservation #{}",
                    reservationId);

            // ✅ CORRECTION : Utiliser la clé admin depuis application.yml
            Map<String, String> checkoutRequest = new HashMap<>();
            checkoutRequest.put("userWalletPrivateKey", adminPrivateKey);

            log.info("🔑 Utilisation de la clé admin configurée");

            // Appeler avec 2 paramètres
            BlockchainTransactionResponse result =
                    blockchainServiceClient.releaseEscrow(reservationId, checkoutRequest);

            if (result != null &&
                    result.getSuccess() != null &&
                    result.getSuccess()) {

                log.info("✅ Escrow libéré via blockchain pour réservation #{}", reservationId);
                log.info("   Transaction: {}", result.getTransactionHash());
                return result;
            } else {
                log.warn("⚠️ Échec libération escrow blockchain: {}",
                        result != null ? result.getError() : "Réponse nulle");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Erreur libération escrow blockchain: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Envoyer une notification
     */
    public void sendNotification(NotificationRequest request) {
        try {
            log.info("📧 Envoi de notification à l'utilisateur #{} - Type: {}",
                    request.getUserId(), request.getNotificationType());

            // S'assurer que le titre n'est pas null
            if (request.getTitle() == null) {
                // Donnez un titre par défaut basé sur le type
                request.setTitle(getDefaultTitle(request.getNotificationType()));
            }

            Map<String, Object> response = notificationServiceClient.createNotification(request);

            if (response != null && response.containsKey("status")) {
                log.info("✅ Notification envoyée avec succès à l'utilisateur #{}",
                        request.getUserId());
            } else {
                log.warn("⚠️ Échec de l'envoi de notification - Réponse: {}", response);
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de notification: {}", e.getMessage());
        }
    }

    private String getDefaultTitle(String notificationType) {
        if (notificationType == null) return "Notification";
        switch (notificationType.toUpperCase()) {
            case "PAYMENT_RECEIVED":
                return "Paiement reçu";
            case "PAYMENT_FAILED":
                return "Échec du paiement";
            case "BOOKING_CONFIRMATION":
                return "Réservation confirmée";
            case "BOOKING_CANCELLED":
                return "Réservation annulée";
            case "CHECK_IN_REMINDER":
                return "Rappel check-in";
            case "MESSAGE_RECEIVED":
                return "Nouveau message";
            default:
                return "Notification";
        }
    }

    /**
     * Méthodes spécifiques pour différents types de notifications
     */
    public void sendPaymentNotification(NotificationRequest request) {
        request.setNotificationType("PAYMENT");
        sendNotification(request);
    }

    public void sendEscrowNotification(NotificationRequest request) {
        request.setNotificationType("ESCROW");
        sendNotification(request);
    }

    public void sendRefundNotification(NotificationRequest request) {
        request.setNotificationType("REFUND");
        sendNotification(request);
    }

    /**
     * Vérifier la santé du service blockchain
     */
    public Map<String, Object> checkBlockchainHealth() {
        try {
            log.info("🩺 Vérification de la santé du service blockchain");

            Map<String, Object> response = blockchainServiceClient.checkBlockchainStatus();

            if (response != null) {
                log.info("✅ Service blockchain en bonne santé");
                return response;
            } else {
                log.warn("⚠️ Service blockchain problématique - réponse nulle");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Erreur vérification santé blockchain: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Vérifier la santé de booking service
     */
    public Map<String, Object> checkBookingServiceHealth() {
        try {
            log.info("🩺 Vérification de la santé du service booking");

            Map<String, Object> response = bookingServiceClient.healthCheck();

            if (response != null && !response.isEmpty()) {
                log.info("✅ Service booking en bonne santé: {}", response);
                return response;
            } else {
                log.warn("⚠️ Service booking problématique - réponse vide ou nulle");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Erreur vérification santé booking: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Vérifier la santé du service notification
     */
    public Map<String, Object> checkNotificationServiceHealth() {
        try {
            log.info("🩺 Vérification de la santé du service notification");

            Map<String, Object> response = notificationServiceClient.healthCheck();

            if (response != null) {
                log.info("✅ Service notification en bonne santé");
                return response;
            } else {
                log.warn("⚠️ Service notification problématique - réponse nulle");
                return Map.of("status", "UNKNOWN", "message", "Réponse nulle");
            }

        } catch (Exception e) {
            log.error("❌ Erreur vérification santé notification: {}", e.getMessage());
            return Map.of("status", "DOWN", "message", e.getMessage());
        }
    }

    /**
     * Récupérer les notifications d'un utilisateur
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public List<Map<String, Object>> getUserNotifications(Integer userId) {
        try {
            log.info("📧 Récupération des notifications pour l'utilisateur #{}", userId);

            Object response = notificationServiceClient.getUserNotifications(userId);

            log.info("📊 Type de réponse reçue: {}", response != null ? response.getClass().getName() : "null");
            log.info("📊 Valeur de réponse: {}", response);

            if (response != null) {
                // Vérifier si c'est déjà une List<Map>
                if (response instanceof List) {
                    List<?> list = (List<?>) response;
                    log.info("📊 Taille de la liste: {}", list.size());

                    if (!list.isEmpty()) {
                        log.info("📊 Premier élément type: {}", list.get(0).getClass().getName());
                        log.info("📊 Premier élément: {}", list.get(0));
                    }

                    // Essayer de convertir
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> result = (List<Map<String, Object>>) response;
                        log.info("✅ Conversion directe réussie");
                        return result;
                    } catch (ClassCastException e) {
                        log.warn("⚠️ Impossible de convertir directement: {}", e.getMessage());

                        // Essayer avec ObjectMapper
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            String json = mapper.writeValueAsString(response);
                            log.info("📊 JSON à convertir: {}", json.substring(0, Math.min(200, json.length())));

                            List<Map<String, Object>> converted = mapper.readValue(json,
                                    new TypeReference<List<Map<String, Object>>>() {
                                    });
                            log.info("✅ Conversion via ObjectMapper réussie");
                            return converted;
                        } catch (Exception jsonError) {
                            log.error("❌ Erreur conversion JSON: {}", jsonError.getMessage());
                        }
                    }
                } else {
                    log.warn("⚠️ La réponse n'est pas une List, c'est: {}", response.getClass());
                }
            } else {
                log.warn("⚠️ Échec récupération notifications - réponse nulle");
            }

            return List.of();

        } catch (Exception e) {
            log.error("❌ Erreur récupération notifications: {}", e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Créer un nouveau wallet via blockchain service
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public Map<String, Object> createWallet() {
        try {
            log.info("🔐 Création d'un nouveau wallet via blockchain service");

            Map<String, Object> result = blockchainServiceClient.createWallet();

            if (result != null) {
                log.info("✅ Wallet créé: {}", result.get("address"));
                return result;
            } else {
                log.warn("⚠️ Échec création wallet - réponse nulle");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Erreur création wallet: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check-in d'une réservation sur la blockchain
     * Note: Cette opération est normalement signée par l'utilisateur via MetaMask
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public BlockchainTransactionResponse checkIn(Long reservationId, String userPrivateKey) {
        try {
            log.info("🔗 [Tentative] Check-in sur blockchain pour réservation #{}", reservationId);

            Map<String, String> request = Map.of("userWalletPrivateKey", userPrivateKey);

            BlockchainTransactionResponse result =
                    blockchainServiceClient.checkIn(reservationId, request);

            if (result != null && result.getSuccess() != null && result.getSuccess()) {
                log.info("✅ Check-in effectué pour réservation #{}, TX: {}",
                        reservationId, result.getTransactionHash());
                return result;
            } else {
                log.warn("⚠️ Échec check-in - Réponse: {}",
                        result != null ? result.getMessage() : "Réponse nulle");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Erreur check-in: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check-out d'une réservation sur la blockchain
     * Note: Cette opération est normalement signée par l'utilisateur via MetaMask
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public BlockchainTransactionResponse checkOut(Long reservationId, String userPrivateKey) {
        try {
            log.info("🔗 [Tentative] Check-out sur blockchain pour réservation #{}", reservationId);

            Map<String, String> request = Map.of("userWalletPrivateKey", userPrivateKey);

            BlockchainTransactionResponse result =
                    blockchainServiceClient.checkOut(reservationId, request);

            if (result != null && result.getSuccess() != null && result.getSuccess()) {
                log.info("✅ Check-out effectué pour réservation #{}, TX: {}",
                        reservationId, result.getTransactionHash());
                return result;
            } else {
                log.warn("⚠️ Échec check-out - Réponse: {}",
                        result != null ? result.getMessage() : "Réponse nulle");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Erreur check-out: {}", e.getMessage());
            return null;
        }
    }
}