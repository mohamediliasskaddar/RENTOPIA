package com.realestate.listing.dto;

import lombok.*;

/**
 * DTO pour la requête de prédiction de prix (nouvelle propriété)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricePredictionRequestDTO {
    private Double surfaceArea;
    private Integer bedrooms;
    private Integer amenitiesCount;
}
