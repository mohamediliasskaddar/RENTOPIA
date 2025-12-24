package com.rentaldapp.bookingservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour recevoir les données de Property Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDTO {
    private Integer propertyId;
    private Integer userId;  // ID du propriétaire
    private String title;
    private String description;
    private String propertyType;
    private String city;
    private String country;

    // Capacités
    private Integer maxGuests;
    private Integer bedrooms;
    private Integer beds;
    private Integer bathrooms;

    // Prix
    private Double weekendPricePerNight;
    private Double weeklyPrice;
    private Double monthlyPrice;
    private Double cleaningFee;
    private Double petFee;
    private Double platformFeePercentage;

    // Règles de réservation
    private Integer minStayNights;
    private Integer maxStayNights;
    private Integer bookingAdvanceDays;
    private Boolean instantBooking;
    private String cancellationPolicy;

    // Horaires
    private String checkInTimeStart;
    private String checkInTimeEnd;
    private String checkOutTime;

    // Statut
    private String status;

    // Blockchain
    private String blockchainPropertyId;
    private String blockchainTxHash;
}