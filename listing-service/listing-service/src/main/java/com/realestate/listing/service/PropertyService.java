// src/main/java/com/realestate/listing/service/PropertyService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.*;
import com.realestate.listing.entity.Property.PropertyStatus;
import com.realestate.listing.repository.DiscountRepository;
import com.realestate.listing.repository.PropertyRepository;
import com.realestate.listing.repository.PropertyVersionRepository;
import com.realestate.listing.service.PropertyVersionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Objects ;

@Service
@Transactional
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private PropertyVersionRepository  versionRepository;

    @Autowired
    private PropertyVersionService propertyVersionService;

    @Autowired
    private PriceHistoryService priceHistoryService;
    // ======================
    // === CRUD DE BASE ===
    // ======================


    public Page<Property> getAllProperties(Pageable pageable) {
        return propertyRepository.findByStatus(Property.PropertyStatus.ACTIVE, pageable);
    }
    public Optional<Property> getPropertyById(Integer id) {
        return propertyRepository.findById(id);
    }

    public Property createProperty(Property property) {
        // Anti-doublon
        if (propertyRepository.existsByTitleAndCityAndAdresseLine(
                property.getTitle(),
                property.getCity(),
                property.getAdresseLine())) {
            throw new IllegalArgumentException(
                    "A property with this title, city, and address already exists.");
        }
        property.setStatus(Property.PropertyStatus.DRAFT); // Forcé
        return propertyRepository.save(property);
    }


    @Transactional
    public Property updateProperty(Integer id, Property updatedProperty) {
        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));

        boolean wasActive = existing.getStatus() == Property.PropertyStatus.ACTIVE;

        // === SAUVEGARDER LES ANCIENS PRIX AVANT TOUTE MODIFICATION ===
        Double oldPrice = existing.getPricePerNight();
        Double oldWeekendPrice = existing.getWeekendPricePerNight();

        // === MISE À JOUR DES CHAMPS SIMPLES ===
        BeanUtils.copyProperties(updatedProperty, existing,
                "propertyId", "status", "createdAt", "updatedAt",
                "amenities", "photos", "availabilities", "discounts", "rules", "hostPreferences", "priceHistory");

        // === COLLECTIONS : UNIQUEMENT SI PRÉSENTES ===
        if (updatedProperty.getAmenities() != null) {
            existing.getAmenities().clear();
            existing.getAmenities().addAll(updatedProperty.getAmenities());
        }

        if (updatedProperty.getDiscounts() != null) {
            existing.getDiscounts().clear();
            existing.getDiscounts().addAll(updatedProperty.getDiscounts());
        }

        if (updatedProperty.getPhotos() != null) {
            existing.getPhotos().clear();
            updatedProperty.getPhotos().forEach(photo -> photo.setProperty(existing));
            existing.getPhotos().addAll(updatedProperty.getPhotos());
        }

        if (updatedProperty.getRules() != null) {
            PropertyRule newRules = updatedProperty.getRules();
            newRules.setProperty(existing);
            existing.setRules(newRules);
        }

        if (updatedProperty.getHostPreferences() != null) {
            HostInteractionPreference prefs = updatedProperty.getHostPreferences();
            prefs.setProperty(existing);
            existing.setHostPreferences(prefs);
        }

        // === MISE À JOUR DE LA DATE ===
        existing.setUpdatedAt(LocalDateTime.now());

        // === SAUVEGARDE ===
        Property saved = propertyRepository.save(existing);

        // === HISTORIQUE DES PRIX (UNIQUEMENT SI ÉTAIT ACTIVE) ===
        if (wasActive) {
            // Prix normal changé
            if (!Objects.equals(oldPrice, saved.getPricePerNight())) {
                priceHistoryService.recordPriceChange(saved, oldPrice, saved.getPricePerNight(), "Night price");
            }

            // Prix weekend changé
            if (!Objects.equals(oldWeekendPrice, saved.getWeekendPricePerNight())) {
                priceHistoryService.recordPriceChange(saved, oldWeekendPrice, saved.getWeekendPricePerNight(), "Weekend price");
            }

            // Créer nouvelle version
            int nextVersion = propertyVersionService.getCurrentVersion(id)
                    .map(v -> v.getNumVersion() + 1)
                    .orElse(1);
            propertyVersionService.createVersionFromProperty(saved, nextVersion);
        }

        return saved;
    }
    public void deleteProperty(Integer id) {
        if (!propertyRepository.existsById(id)) {
            throw new IllegalArgumentException("Property not found: " + id);
        }
        propertyRepository.deleteById(id);
    }

    // ==========================
    // === RECHERCHES SIMPLES ===
    // ==========================

    public List<Property> getByUserId(Integer userId) {
        return propertyRepository.findByUserId(userId);
    }
    public List<Property> getAllByUserId(Integer userId) {
        return propertyRepository.findByUserId(userId); // Tous les statuts
    }

    public List<Property> getByUserIdAndStatus(Integer userId, PropertyStatus status) {
        return propertyRepository.findByUserIdAndStatus(userId, status);
    }
    public Optional<Property> getByPropertyIdAndUserId(Integer propertyId, Integer userId) {
        return propertyRepository.findByPropertyIdAndUserId(propertyId, userId);
    }

    public List<Property> getByStatus(PropertyStatus status) {
        return propertyRepository.findByStatus(status);
    }

    // ================================
    // === RECHERCHE AVANCÉE (FILTRES) ===
    // ================================

    public List<Property> searchProperties(
            String city, String country, String propertyType, String placeType,
            Double minPrice, Double maxPrice,
            Integer bedrooms, Integer bathrooms, Integer beds, Integer maxGuests,
            Boolean instantBooking, List<Integer> amenityIds,
            Boolean childrenAllowed, Boolean babiesAllowed, Boolean petsAllowed,
            Boolean smokingAllowed, Boolean eventsAllowed) {

        // ✅ AJOUT : Calculer le nombre d'amenities demandés
        Integer amenityCount = (amenityIds != null && !amenityIds.isEmpty()) ? amenityIds.size() : null;

        return propertyRepository.searchProperties(
                city, country, propertyType, placeType,
                minPrice, maxPrice, bedrooms, bathrooms, beds,
                maxGuests, instantBooking, amenityIds, amenityCount, // ← Ajout de amenityCount
                childrenAllowed, babiesAllowed, petsAllowed,
                smokingAllowed, eventsAllowed
        );
    }


    // ==========================
    // === PRÉVENTION DOUBLON ===
    // ==========================

    public boolean existsByTitleAndUserIdAndCityAndAdresseLine(
            String title, Integer userId, String city, String adresseLine) {
        return propertyRepository.existsByTitleAndCityAndAdresseLine(title, city, adresseLine);
    }


    // calcul de prix
    public double calculateTotalPrice(
            Property property,
            LocalDate checkIn,
            LocalDate checkOut,
            boolean isFirstBooking,
            LocalDate bookingDate
    ) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) throw new IllegalArgumentException("Dates invalides");

        // === PRIX PAR DÉFAUT ===
        double basePrice = property.getPricePerNight() != null ? property.getPricePerNight() : 0.0;
        double weekendPrice = property.getWeekendPricePerNight() != null
                ? property.getWeekendPricePerNight()
                : basePrice; // ← SI NULL → UTILISE LE PRIX NORMAL

        // === CALCUL PRIX DE BASE ===
        double baseTotal = 0.0;
        LocalDate date = checkIn;
        while (!date.isAfter(checkOut.minusDays(1))) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
                baseTotal += weekendPrice;
            } else {
                baseTotal += basePrice;
            }
            date = date.plusDays(1);
        }

        // === RÉDUCTIONS ===
        double bestDiscount = 0.0;
        for (Discount d : property.getDiscounts()) {
            if (d.getMinNights() != null && nights < d.getMinNights()) continue;
            boolean applies = switch (d.getDiscountType()) {
                case "last_minute" -> ChronoUnit.DAYS.between(bookingDate, checkIn) <= 14;
                case "weekly", "monthly" -> true;
                default -> false;
            };
            if (applies && d.getDiscountPercentage() > bestDiscount) {
                bestDiscount = d.getDiscountPercentage();
            }
        }

        // === PRIX FINAL ===
        return baseTotal * (1 - bestDiscount / 100.0);
    }


    @Transactional
    public Property publishProperty(Integer propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (property.getStatus() == Property.PropertyStatus.ACTIVE) {
            throw new IllegalArgumentException("Already published");
        }

        // 1. Passer en ACTIVE
        property.setStatus(Property.PropertyStatus.ACTIVE);
        propertyRepository.save(property);

        // 2. Créer Version 1
        propertyVersionService.createVersionFromProperty(property, 1);

        return property;
    }


}