// src/main/java/com/realestate/listing/mapper/PropertyCardMapper.java
package com.realestate.listing.mapper;

import com.realestate.listing.dto.PropertyCardDTO;
import com.realestate.listing.entity.Property;
import com.realestate.listing.entity.PropertyPhoto;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class PropertyCardMapper {

    public PropertyCardDTO toCardDTO(Property property) {
        if (property == null) {
            return null;
        }

        // Trouver la photo principale (celle avec displayOrder = 1, ou la première)
        String mainPhotoUrl = property.getPhotos().stream()
                .min(Comparator.comparing(PropertyPhoto::getDisplayOrder))
                .map(PropertyPhoto::getPhotoUrl)
                .orElse(null);

        return PropertyCardDTO.builder()
                .propertyId(property.getPropertyId())
                .title(property.getTitle())
                .city(property.getCity())
                .country(property.getCountry())
                .propertyType(property.getPropertyType())
                .placeType(property.getPlaceType())
                .maxGuests(property.getMaxGuests())
                .bedrooms(property.getBedrooms())
                .beds(property.getBeds())
                .bathrooms(property.getBathrooms())
                .pricePerNight(property.getPricePerNight())
                .weekendPricePerNight(property.getWeekendPricePerNight())
                .mainPhotoUrl(mainPhotoUrl)
                // TODO: Ajouter averageRating et reviewCount quand le système de reviews sera prêt
                .averageRating(null)
                .reviewCount(null)
                .build();
    }
}