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
    @PostMapping("/payments/create")
    PaymentResponseDTO createPayment(@RequestBody Map<String, Object> paymentRequest);

    /**
     * Initier un remboursement
     */
    @PostMapping("/payments/refund")
    void initiateRefund(
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("reason") String reason
    );

    /**
     * Libérer l'escrow au propriétaire
     */
    @PostMapping("/payments/escrow/release")
    void releaseEscrow(
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("hostWallet") String hostWallet
    );

    /**
     * ✅ NOUVEAU : Vérifier le statut d'une transaction blockchain
     */
    @GetMapping("/payments/transaction/{txHash}/status")
    String getTransactionStatus(@PathVariable("txHash") String txHash);

    /**
     * ✅ NOUVEAU : Annuler un paiement
     */
    @PostMapping("/payments/cancel")
    void cancelPayment(@RequestParam("reservationId") Integer reservationId);

    /**
     * ✅ NOUVEAU : Récupérer les détails d'un paiement
     */
    @GetMapping("/payments/reservation/{reservationId}")
    PaymentResponseDTO getPaymentByReservation(@PathVariable("reservationId") Integer reservationId);
}