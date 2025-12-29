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
public class ReleaseEscrowCommand {

    @NotNull(message = "L'ID de réservation est obligatoire")
    private Integer reservationId;

    @NotBlank(message = "L'adresse wallet du locataire est obligatoire")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Adresse Ethereum invalide")
    private String tenantWallet;

    @NotBlank(message = "L'adresse wallet du propriétaire est obligatoire")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Adresse Ethereum invalide")
    private String hostWallet;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.001", message = "Le montant minimum est 0.001 ETH")
    private Double amountEth;

    @NotBlank(message = "La raison est obligatoire")
    private String reason; // CHECK_OUT_COMPLETED, EARLY_RELEASE, etc.
}