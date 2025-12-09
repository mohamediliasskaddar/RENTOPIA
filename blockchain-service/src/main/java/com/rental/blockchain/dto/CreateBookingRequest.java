package com.rental.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Requête pour créer une réservation sur la blockchain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Property ID est requis")
    @Positive(message = "Property ID doit être positif")
    private Long propertyId;

    @NotNull(message = "Start date est requis")
    @Positive(message = "Start date doit être positif")
    private Long startDate; // Timestamp Unix en secondes

    @NotNull(message = "End date est requis")
    @Positive(message = "End date doit être positif")
    private Long endDate; // Timestamp Unix en secondes

    @NotNull(message = "Rental amount est requis")
    @Positive(message = "Rental amount doit être positif")
    private BigDecimal rentalAmount; // Montant en ETH

    @NotNull(message = "User wallet private key est requis")
    private String userWalletPrivateKey; // Clé privée de l'utilisateur
}