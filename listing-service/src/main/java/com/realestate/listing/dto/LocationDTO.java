package com.realestate.listing.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================
 * DTO LOCATION
 * Représente une ville/pays pour l'autocomplete
 * ============================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {

    private String city;
    private String country;
    private String displayName;


    /**
     * Constructeur avec génération automatique du displayName
     */
    public LocationDTO(String city, String country, Long propertyCount) {
        this.city = city;
        this.country = country;
        this.displayName = city + ", " + country;

    }

    /**
     * Constructeur simple
     */
    public LocationDTO(String city, String country) {
        this.city = city;
        this.country = country;
        this.displayName = city + ", " + country;
    }
}