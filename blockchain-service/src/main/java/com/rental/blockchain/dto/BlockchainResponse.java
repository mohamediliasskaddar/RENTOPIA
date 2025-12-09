package com.rental.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse blockchain générique
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainResponse {

    private boolean success;
    private String message;
    private Long blockchainBookingId;
    private String txHash;
    private Long blockNumber;
    private String etherscanLink;
    private String error;

    /**
     * Créer une réponse de succès (avec booking ID)
     */
    public static BlockchainResponse success(
            Long blockchainBookingId,
            String txHash,
            Long blockNumber
    ) {
        return BlockchainResponse.builder()
                .success(true)
                .message("Transaction réussie")
                .blockchainBookingId(blockchainBookingId)
                .txHash(txHash)
                .blockNumber(blockNumber)
                .etherscanLink("https://sepolia.etherscan.io/tx/" + txHash)
                .build();
    }

    /**
     * Créer une réponse de succès (sans booking ID)
     */
    public static BlockchainResponse success(String txHash, Long blockNumber) {
        return BlockchainResponse.builder()
                .success(true)
                .message("Transaction réussie")
                .txHash(txHash)
                .blockNumber(blockNumber)
                .etherscanLink("https://sepolia.etherscan.io/tx/" + txHash)
                .build();
    }

    /**
     * Créer une réponse d'erreur
     */
    public static BlockchainResponse error(String errorMessage) {
        return BlockchainResponse.builder()
                .success(false)
                .message("Erreur blockchain")
                .error(errorMessage)
                .build();
    }
}