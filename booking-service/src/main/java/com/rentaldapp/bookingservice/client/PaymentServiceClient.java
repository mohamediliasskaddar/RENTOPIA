package com.rentaldapp.bookingservice.client;

import com.rentaldapp.bookingservice.model.dto.PaymentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Client Feign pour communiquer avec le Payment Service
 * ✅ CORRECTION : URL corrigée + méthodes manquantes ajoutées
 */
@FeignClient(name = "payment-service", url = "${services.payment-service.url:http://localhost:8084}")
public interface PaymentServiceClient {

    /**
     * ✅ NOUVEAU : Créer un paiement initial (lors de la confirmation)
     */
    @PostMapping("/api/v1/payments/create")
    PaymentResponseDTO createPayment(@RequestBody Map<String, Object> paymentRequest);

    /**
     * Initier un remboursement
     */
    @PostMapping("/api/v1/payments/refund")
    void initiateRefund(
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("reason") String reason
    );

    /**
     * Libérer l'escrow au propriétaire
     */
    @PostMapping("/api/v1/payments/escrow/release")
    void releaseEscrow(
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("hostWallet") String hostWallet
    );

    /**
     * ✅ NOUVEAU : Vérifier le statut d'une transaction blockchain
     */
    @GetMapping("/api/v1/payments/transaction/{txHash}/status")
    String getTransactionStatus(@PathVariable("txHash") String txHash);

    /**
     * ✅ NOUVEAU : Annuler un paiement
     */
    @PostMapping("/api/v1/payments/cancel")
    void cancelPayment(@RequestParam("reservationId") Integer reservationId);

    /**
     * ✅ NOUVEAU : Récupérer les détails d'un paiement
     */
    @GetMapping("/api/v1/payments/reservation/{reservationId}")
    PaymentResponseDTO getPaymentByReservation(@PathVariable("reservationId") Integer reservationId);
}