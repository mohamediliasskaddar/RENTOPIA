package com.realestate.listing.dto;


import lombok.*;

/**
 * DTO léger pour afficher les properties dans la liste (cards)
 * Contient uniquement les infos essentielles + 1 photo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyCardDTO {
    private Integer propertyId;
    private String title;
    private String city;
    private String country;
    private String propertyType;
    private String placeType;

    private Integer maxGuests;
    private Integer bedrooms;
    private Integer beds;
    private Integer bathrooms;

    private Double pricePerNight;
    private Double weekendPricePerNight;

    // Photo principale (la 1ère dans l'ordre)
    private String mainPhotoUrl;

    // Optionnel : note moyenne (si vous avez un système de reviews)
    private Double averageRating;
    private Integer reviewCount;
    private String status;
}