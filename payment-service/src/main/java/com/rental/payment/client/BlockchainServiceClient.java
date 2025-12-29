package com.rental.payment.client;

import com.rental.payment.config.FeignConfig;
import com.rental.payment.dto.BlockchainTransactionResponse; // ✅ Utiliser votre DTO
import com.rental.payment.dto.CreateBookingBlockchainRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "BLOCKCHAIN-SERVICE",
        configuration = FeignConfig.class,
        fallback = BlockchainServiceClientFallback.class
)
public interface BlockchainServiceClient {

    /**
     * Créer une réservation sur la blockchain
     * POST /api/blockchain/bookings/create
     */
    @PostMapping("/api/blockchain/bookings/create")
    BlockchainTransactionResponse createBookingTransaction(
            @RequestBody CreateBookingBlockchainRequest request
    );

    /**
     * ✅ CORRIGÉ : Libérer les fonds d'une réservation (Admin)
     * Appeler checkout() au lieu de release-funds
     */
    @PostMapping("/api/blockchain/bookings/{id}/release-funds")
    BlockchainTransactionResponse releaseEscrow(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> request  // ✅ AJOUTER le paramètre request
    );

    /**
     * Check-in d'une réservation
     */
    @PostMapping("/api/blockchain/bookings/{id}/checkin")
    BlockchainTransactionResponse checkIn(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> request
    );

    /**
     * Check-out d'une réservation
     */
    @PostMapping("/api/blockchain/bookings/{id}/checkout")
    BlockchainTransactionResponse checkOut(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> request
    );

    /**
     * Créer un nouveau wallet
     */
    @PostMapping("/api/blockchain/wallets/create")
    Map<String, Object> createWallet();

    /**
     * Vérifier l'état du service blockchain
     */
    @GetMapping("/api/blockchain/health")
    Map<String, Object> checkBlockchainStatus();
}