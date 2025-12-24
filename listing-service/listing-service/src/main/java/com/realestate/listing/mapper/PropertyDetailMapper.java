// src/main/java/com/realestate/listing/mapper/PropertyDetailMapper.java
package com.realestate.listing.mapper;

import com.realestate.listing.dto.PropertyDetailDTO;
import com.realestate.listing.entity.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir Property en PropertyDetailDTO
 * Inclut toutes les relations (photos, rules, hostPreferences)
 */
@Component
public class PropertyDetailMapper {

    public PropertyDetailDTO toDTO(Property property) {
        if (property == null) {
            return null;
        }

        return PropertyDetailDTO.builder()
                // Informations de base
                .propertyId(property.getPropertyId())
                .userId(property.getUserId())
                .title(property.getTitle())
                .description(property.getDescription())
                .propertyType(property.getPropertyType())
                .placeType(property.getPlaceType())

                // Localisation
                .adresseLine(property.getAdresseLine())
                .city(property.getCity())
                .country(property.getCountry())
                .postalCode(property.getPostalCode())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .neighborhoodDescription(property.getNeighborhoodDescription())

                // Caractéristiques
                .floorNumber(property.getFloorNumber())
                .surfaceArea(property.getSurfaceArea())
                .maxGuests(property.getMaxGuests())
                .bedrooms(property.getBedrooms())
                .beds(property.getBeds())
                .bathrooms(property.getBathrooms())

                // Prix
                .pricePerNight(property.getPricePerNight())
                .weekendPricePerNight(property.getWeekendPricePerNight())
                .cleaningFee(property.getCleaningFee())
                .petFee(property.getPetFee())
                .platformFeePercentage(property.getPlatformFeePercentage())

                // Règles de réservation
                .minStayNights(property.getMinStayNights())
                .maxStayNights(property.getMaxStayNights())
                .bookingAdvanceDays(property.getBookingAdvanceDays())
                .checkInTimeStart(property.getCheckInTimeStart())
                .checkInTimeEnd(property.getCheckInTimeEnd())
                .checkOutTime(property.getCheckOutTime())
                .instantBooking(property.getInstantBooking())
                .cancellationPolicy(property.getCancellationPolicy())

                // Status
                .status(property.getStatus() != null ? property.getStatus().name() : null)

                // Blockchain
                .blockchainPropertyId(property.getBlockchainPropertyId())
                .blockchainTxHash(property.getBlockchainTxHash())

                // Dates
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())

                // Relations
                .amenities(mapAmenities(property.getAmenities()))
                .photos(mapPhotos(property.getPhotos()))
                .discounts(mapDiscounts(property.getDiscounts()))
                .rules(mapRules(property.getRules()))
                .hostPreferences(mapHostPreferences(property.getHostPreferences()))

                .build();
    }

    // === MAPPERS DES RELATIONS ===

    private Set<PropertyDetailDTO.AmenityDTO> mapAmenities(Set<Amenity> amenities) {
        if (amenities == null) {
            return Set.of();
        }
        return amenities.stream()
                .map(a -> PropertyDetailDTO.AmenityDTO.builder()
                        .amenityId(a.getAmenityId())
                        .name(a.getName())
                        .icon(a.getIcone())  // ✅ icone (pas icon)
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<PropertyDetailDTO.PropertyPhotoDTO> mapPhotos(Set<PropertyPhoto> photos) {
        if (photos == null) {
            return Set.of();
        }
        return photos.stream()
                .map(p -> PropertyDetailDTO.PropertyPhotoDTO.builder()
                        .photoId(p.getPhotoId())
                        .photoUrl(p.getPhotoUrl())
                        .displayOrder(p.getDisplayOrder())
                        .caption(null)  // ✅ Pas de caption dans PropertyPhoto
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<PropertyDetailDTO.DiscountDTO> mapDiscounts(Set<Discount> discounts) {
        if (discounts == null) {
            return Set.of();
        }
        return discounts.stream()
                .map(d -> PropertyDetailDTO.DiscountDTO.builder()
                        .discountId(d.getDiscountId())
                        .discountType(d.getDiscountType())
                        .discountPercentage(d.getDiscountPercentage())
                        .minNights(d.getMinNights())
                        .build())
                .collect(Collectors.toSet());
    }

    private PropertyDetailDTO.PropertyRuleDTO mapRules(PropertyRule rules) {
        if (rules == null) {
            return null;
        }
        return PropertyDetailDTO.PropertyRuleDTO.builder()
                .childrenAllowed(rules.getChildrenAllowed())
                .babiesAllowed(rules.getBabiesAllowed())
                .petsAllowed(rules.getPetsAllowed())
                .smokingAllowed(rules.getSmokingAllowed())
                .eventsAllowed(rules.getEventsAllowed())
                .additionalRules(rules.getCustomRules())  // ✅ customRules (pas additionalRules)
                .build();
    }

    private PropertyDetailDTO.HostInteractionPreferenceDTO mapHostPreferences(HostInteractionPreference prefs) {
        if (prefs == null) {
            return null;
        }
        return PropertyDetailDTO.HostInteractionPreferenceDTO.builder()
                .communicationStyle(prefs.getInteractionLevel())  // ✅ interactionLevel
                .responseTime(null)  // ✅ Pas de responseTime dans l'entité
                .languagesSpoken(null)  // ✅ Pas de languagesSpoken dans l'entité
                .checkInProcess(prefs.getCheckInMethod() + " - " +
                        (prefs.getCheckInInstructions() != null ? prefs.getCheckInInstructions() : ""))  // ✅ Combiné
                .build();
    }
}