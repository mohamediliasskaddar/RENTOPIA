package com.rental.payment.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundCommand {

    @NotNull(message = "L'ID de réservation est obligatoire")
    private Integer reservationId;

    @NotBlank(message = "L'adresse wallet du locataire est obligatoire")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Adresse Ethereum invalide")
    private String tenantWallet;

    @NotNull(message = "Le montant du remboursement est obligatoire")
    @DecimalMin(value = "0.001", message = "Le montant minimum est 0.001 ETH")
    private Double refundAmountEth;

    @NotBlank(message = "La raison est obligatoire")
    private String reason; // CANCELLATION, DISPUTE_RESOLVED, etc.
}