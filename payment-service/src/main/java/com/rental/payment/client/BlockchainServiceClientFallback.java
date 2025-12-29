package com.rental.payment.client;

import com.rental.payment.dto.BlockchainTransactionResponse;
import com.rental.payment.dto.CreateBookingBlockchainRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BlockchainServiceClientFallback implements BlockchainServiceClient {

    @Override
    public BlockchainTransactionResponse createBookingTransaction(CreateBookingBlockchainRequest request) {
        log.error("❌ Fallback: Blockchain Service indisponible pour créer une transaction.");

        return BlockchainTransactionResponse.builder()
                .status("SERVICE_UNAVAILABLE")
                .success(false)
                .error("Service blockchain indisponible")
                .message("Transaction simulée en fallback")
                .transactionHash("simulated_tx_hash_" + System.currentTimeMillis())
                .blockchainBookingId(9999L)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public BlockchainTransactionResponse releaseEscrow(Long id, Map<String, String> request) {  // ✅ AJOUTER request
        log.error("❌ Fallback: Impossible de libérer l'escrow pour réservation #{}", id);

        return BlockchainTransactionResponse.builder()
                .status("SERVICE_UNAVAILABLE")
                .success(false)
                .error("Service blockchain indisponible")
                .message("Impossible de libérer l'escrow")
                .timestamp(LocalDateTime.now())
                .build();
    }


    @Override
    public BlockchainTransactionResponse checkIn(Long id, Map<String, String> request) {
        log.error("❌ Fallback: Impossible de check-in pour réservation #{}", id);

        return BlockchainTransactionResponse.builder()
                .status("SERVICE_UNAVAILABLE")
                .success(false)
                .error("Service blockchain indisponible")
                .message("Check-in impossible")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public BlockchainTransactionResponse checkOut(Long id, Map<String, String> request) {
        log.error("❌ Fallback: Impossible de check-out pour réservation #{}", id);

        return BlockchainTransactionResponse.builder()
                .status("SERVICE_UNAVAILABLE")
                .success(false)
                .error("Service blockchain indisponible")
                .message("Check-out impossible")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public Map<String, Object> createWallet() {
        log.error("❌ Fallback: Impossible de créer un wallet");

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Service blockchain indisponible");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @Override
    public Map<String, Object> checkBlockchainStatus() {
        log.warn("⚠️ Fallback: Impossible de vérifier le statut blockchain");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "DOWN");
        response.put("message", "Service blockchain indisponible");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}