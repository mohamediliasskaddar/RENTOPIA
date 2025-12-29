package com.rental.payment.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor  // ✅ AJOUTER
@AllArgsConstructor // ✅ AJOUTER
public class BlockchainTransactionRequest {

    @NotNull(message = "L'ID de réservation est obligatoire")
    private Integer reservationId;

    @NotBlank(message = "L'adresse source est obligatoire")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Adresse Ethereum invalide") // ✅ AJOUTER
    private String fromAddress;

    @NotBlank(message = "L'adresse destination est obligatoire")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Adresse Ethereum invalide") // ✅ AJOUTER
    private String toAddress;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.001", message = "Le montant minimum est 0.001 ETH")
    @DecimalMax(value = "1000.0", message = "Le montant maximum est 1000 ETH") // ✅ AJOUTER (sécurité)
    private Double amountEth;

    @NotBlank(message = "Le type de paiement est obligatoire")
    @Pattern(regexp = "BOOKING|ESCROW_RELEASE|REFUND",
            message = "Type de paiement invalide. Valeurs acceptées: BOOKING, ESCROW_RELEASE, REFUND")
    private String paymentType;

    @NotBlank(message = "L'adresse du contrat est obligatoire")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Adresse de contrat invalide") // ✅ AJOUTER
    private String contractAddress;

    private String functionName;
    private Object[] functionParams;

    // ✅ AMÉLIORER la méthode de validation
    public boolean isValid() {
        return reservationId != null &&
                isValidEthereumAddress(fromAddress) &&
                isValidEthereumAddress(toAddress) &&
                amountEth != null && amountEth >= 0.001 && amountEth <= 1000.0 &&
                paymentType != null && !paymentType.isEmpty() &&
                isValidEthereumAddress(contractAddress);
    }

    // ✅ AJOUTER une méthode utilitaire
    private boolean isValidEthereumAddress(String address) {
        return address != null &&
                address.matches("^0x[a-fA-F0-9]{40}$");
    }

    // ✅ AJOUTER factory methods
    public static BlockchainTransactionRequest forBooking(
            Integer reservationId,
            String tenantWallet,
            String hostWallet,
            Double amount,
            String contractAddress) {
        return BlockchainTransactionRequest.builder()
                .reservationId(reservationId)
                .fromAddress(tenantWallet)
                .toAddress(hostWallet)
                .amountEth(amount)
                .paymentType("BOOKING")
                .contractAddress(contractAddress)
                .build();
    }

    public static BlockchainTransactionRequest forEscrowRelease(
            Integer reservationId,
            String tenantWallet,
            String hostWallet,
            Double amount,
            String contractAddress) {
        return BlockchainTransactionRequest.builder()
                .reservationId(reservationId)
                .fromAddress(tenantWallet)
                .toAddress(hostWallet)
                .amountEth(amount)
                .paymentType("ESCROW_RELEASE")
                .contractAddress(contractAddress)
                .build();
    }

    public static BlockchainTransactionRequest forRefund(
            Integer reservationId,
            String hostWallet,
            String tenantWallet,
            Double amount,
            String contractAddress) {
        return BlockchainTransactionRequest.builder()
                .reservationId(reservationId)
                .fromAddress(hostWallet)  // Inverse : host → tenant
                .toAddress(tenantWallet)
                .amountEth(amount)
                .paymentType("REFUND")
                .contractAddress(contractAddress)
                .build();
    }
}