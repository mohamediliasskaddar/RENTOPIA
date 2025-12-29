package com.rental.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTransactionResponse {
    private String status;
    private String transactionHash;
    private Long blockchainBookingId;
    private Long blockNumber;
    private Double gasUsed;
    private String message;
    private Boolean success;
    private LocalDateTime timestamp;

    // ✅ AJOUTER CE CHAMP
    private String error;

    // Alias pour compatibilité avec blockchain-service
    public String getTxHash() {
        return transactionHash;
    }

    public void setTxHash(String txHash) {
        this.transactionHash = txHash;
    }

    // Méthode utilitaire
    public static BlockchainTransactionResponse success(String txHash, Long bookingId) {
        return BlockchainTransactionResponse.builder()
                .status("CONFIRMED")
                .transactionHash(txHash)
                .blockchainBookingId(bookingId)
                .success(true)
                .timestamp(LocalDateTime.now())
                .message("Transaction réussie")
                .build();
    }

    public static BlockchainTransactionResponse failed(String errorMessage) {
        return BlockchainTransactionResponse.builder()
                .status("FAILED")
                .success(false)
                .error(errorMessage)
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static BlockchainTransactionResponse serviceUnavailable() {
        return BlockchainTransactionResponse.builder()
                .status("SERVICE_UNAVAILABLE")
                .success(false)
                .error("Service blockchain temporairement indisponible")
                .message("Service indisponible")
                .timestamp(LocalDateTime.now())
                .build();
    }
}