package com.realestate.listing.dto;


import com.realestate.listing.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO pour la page de détails d'une propriété
 * Inclut TOUTES les informations : photos, rules, hostPreferences
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDetailDTO {

    // === INFORMATIONS DE BASE ===
    private Integer propertyId;
    private Integer userId;
    private String title;
    private String description;
    private String propertyType;
    private String placeType;

    // === LOCALISATION ===
    private String adresseLine;
    private String city;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private String neighborhoodDescription;

    // === CARACTÉRISTIQUES ===
    private Integer floorNumber;
    private Double surfaceArea;
    private Integer maxGuests;
    private Integer bedrooms;
    private Integer beds;
    private Integer bathrooms;

    // === PRIX ===
    private Double pricePerNight;
    private Double weekendPricePerNight;
    private Double cleaningFee;
    private Double petFee;
    private Double platformFeePercentage;

    // === RÈGLES DE RÉSERVATION ===
    private Integer minStayNights;
    private Integer maxStayNights;
    private Integer bookingAdvanceDays;
    private String checkInTimeStart;
    private String checkInTimeEnd;
    private String checkOutTime;
    private Boolean instantBooking;
    private String cancellationPolicy;

    // === STATUS ===
    private String status;

    // === BLOCKCHAIN ===
    private String blockchainPropertyId;
    private String blockchainTxHash;

    // === DATES ===
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === RELATIONS (incluses dans ce DTO) ===
    private Set<AmenityDTO> amenities;
    private Set<PropertyPhotoDTO> photos;
    private Set<DiscountDTO> discounts;
    private PropertyRuleDTO rules;
    private HostInteractionPreferenceDTO hostPreferences;

    // === DTOs IMBRIQUÉS ===

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AmenityDTO {
        private Integer amenityId;
        private String name;
        private String icon;
        private String category;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyPhotoDTO {
        private Integer photoId;
        private String photoUrl;
        private Integer displayOrder;
        private String caption;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiscountDTO {
        private Integer discountId;
        private String discountType;
        private Double discountPercentage;
        private Integer minNights;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyRuleDTO {
        private Boolean childrenAllowed;
        private Boolean babiesAllowed;
        private Boolean petsAllowed;
        private Boolean smokingAllowed;
        private Boolean eventsAllowed;
        private String additionalRules;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HostInteractionPreferenceDTO {
        private String communicationStyle;
        private String responseTime;
        private String languagesSpoken;
        private String checkInProcess;
    }
}