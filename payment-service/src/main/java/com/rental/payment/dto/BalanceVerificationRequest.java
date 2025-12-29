package com.rental.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceVerificationRequest {

    @NotBlank(message = "L'adresse wallet est obligatoire")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Adresse Ethereum invalide")
    private String walletAddress;

    @NotNull(message = "Le montant requis est obligatoire")
    @DecimalMin(value = "0.001", message = "Le montant minimum est 0.001 ETH")
    private Double requiredAmountEth;

    // Méthode factory pour les tests
    public static BalanceVerificationRequest of(String walletAddress, Double requiredAmount) {
        return BalanceVerificationRequest.builder()
                .walletAddress(walletAddress)
                .requiredAmountEth(requiredAmount)
                .build();
    }
}