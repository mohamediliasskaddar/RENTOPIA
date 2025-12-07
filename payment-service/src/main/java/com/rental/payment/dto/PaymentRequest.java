package com.rental.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "L'ID de réservation est obligatoire")
    private Integer reservationId;

    @NotNull(message = "L'ID du propriétaire est obligatoire")
    private Integer hostId;

    @NotNull(message = "L'ID du locataire est obligatoire")
    private Integer tenantId;

    @NotBlank(message = "L'adresse wallet du locataire est obligatoire")
    private String tenantWalletAddress;

    @NotBlank(message = "L'adresse wallet du propriétaire est obligatoire")
    private String hostWalletAddress;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.001", message = "Le montant minimum est 0.001 ETH")
    private Double amountEth;

    @NotNull(message = "Le montant total est obligatoire")
    private Double totalAmountEth;
}