package com.realestate.listing.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.realestate.listing.entity.Property;
import com.realestate.listing.entity.PropertyGeneralSnapshot;
import com.realestate.listing.repository.PropertyGeneralSnapshotRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@Transactional
public class PropertyGeneralSnapshotService {

    @Autowired
    private PropertyGeneralSnapshotRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    public PropertyGeneralSnapshot getOrCreateSnapshot(Property property) {
        try {
            // Sérialiser TOUS les champs généraux
            GeneralDTO dto = new GeneralDTO(
                    property.getTitle(),
                    property.getDescription(),
                    property.getPropertyType(),
                    property.getPlaceType(),
                    property.getAdresseLine(),
                    property.getCity(),
                    property.getCountry(),
                    property.getPostalCode(),
                    property.getLatitude(),
                    property.getLongitude(),
                    property.getNeighborhoodDescription(),
                    property.getFloorNumber(),
                    property.getSurfaceArea(),
                    property.getMaxGuests(),
                    property.getBedrooms(),
                    property.getBeds(),
                    property.getBathrooms(),
                    property.getWeekendPricePerNight(),
                    property.getPricePerNight(),
                    property.getCleaningFee(),
                    property.getPetFee(),
                    property.getPlatformFeePercentage(),
                    property.getMinStayNights(),
                    property.getMaxStayNights(),
                    property.getBookingAdvanceDays(),
                    property.getCheckInTimeStart(),
                    property.getCheckInTimeEnd(),
                    property.getCheckOutTime(),
                    property.getInstantBooking(),
                    property.getCancellationPolicy()
            );

            String json = objectMapper.writeValueAsString(dto);
            String hash = Hashing.sha256().hashString(json, StandardCharsets.UTF_8).toString();

            return repo.findBySnapshotHash(hash)
                    .orElseGet(() -> {
                        PropertyGeneralSnapshot snapshot = new PropertyGeneralSnapshot();
                        snapshot.setGeneralJson(json);
                        snapshot.setSnapshotHash(hash);
                        snapshot.setCreatedAt(LocalDateTime.now());
                        return repo.save(snapshot);
                    });

        } catch (Exception e) {
            throw new RuntimeException("Erreur snapshot général", e);
        }
    }
}

// DTO pour éviter boucle infinie
record GeneralDTO(
        String title, String description, String propertyType, String placeType,
        String adresseLine, String city, String country, String postalCode,
        Double latitude, Double longitude, String neighborhoodDescription,
        Integer floorNumber, Double surfaceArea,
        Integer maxGuests, Integer bedrooms, Integer beds, Integer bathrooms,
        Double weekendPricePerNight, Double pricePerNight,
        Double cleaningFee, Double petFee, Double platformFeePercentage,
        Integer minStayNights, Integer maxStayNights, Integer bookingAdvanceDays,
        String checkInTimeStart, String checkInTimeEnd, String checkOutTime,
        Boolean instantBooking, String cancellationPolicy
) {}