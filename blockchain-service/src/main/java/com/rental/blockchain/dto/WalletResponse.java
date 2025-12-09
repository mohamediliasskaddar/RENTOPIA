package com.rental.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse pour la création d'un wallet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private String address;
    private String encryptedPrivateKey;
    private boolean success;
    private String message;

    /**
     * Créer une réponse de succès
     */
    public static WalletResponse success(String address, String encryptedKey) {
        return WalletResponse.builder()
                .success(true)
                .message("Wallet créé avec succès")
                .address(address)
                .encryptedPrivateKey(encryptedKey)
                .build();
    }

    /**
     * Créer une réponse d'erreur
     */
    public static WalletResponse error(String message) {
        return WalletResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}