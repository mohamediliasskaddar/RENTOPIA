// src/main/java/com/realestate/listing/service/PropertyService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.*;
import com.realestate.listing.entity.Property.PropertyStatus;
import com.realestate.listing.repository.*;
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
import java.util.*;
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
    @Autowired
    private AmenityRepository amenityRepository;
    @Autowired
    private PropertyPhotosRepository photoRepository;
    // ======================
    // === CRUD DE BASE ===
    // ======================

    /**
     * ✅ NOUVEAU : Vérifier si un utilisateur est propriétaire d'une propriété
     */
    public boolean isOwner(Integer propertyId, Integer userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Propriété non trouvée"));

        return property.getUserId().equals(userId);
    }
    public Page<Property> getAllProperties(Pageable pageable) {
        return propertyRepository.findByStatus(Property.PropertyStatus.ACTIVE, pageable);
    }
    public Optional<Property> getPropertyById(Integer id) {
        return propertyRepository.findById(id);
    }

    // PropertyService.java

    public Property createProperty(Property property) {
        // Anti-doublon
        if (propertyRepository.existsByTitleAndCityAndAdresseLine(
                property.getTitle(),
                property.getCity(),
                property.getAdresseLine())) {
            throw new IllegalArgumentException(
                    "A property with this title, city, and address already exists.");
        }

        property.setStatus(Property.PropertyStatus.DRAFT);

        // ✅ Gérer PropertyRule - Configurer la relation bidirectionnelle
        if (property.getRules() != null) {
            property.getRules().setProperty(property);
        } else {
            // Créer des règles par défaut si non fournies
            PropertyRule defaultRules = new PropertyRule();
            defaultRules.setProperty(property);
            defaultRules.setChildrenAllowed(true);
            defaultRules.setBabiesAllowed(true);
            defaultRules.setPetsAllowed(false);
            defaultRules.setSmokingAllowed(false);
            defaultRules.setEventsAllowed(false);
            property.setRules(defaultRules);
        }

        // ✅ Gérer HostInteractionPreference - Configurer la relation bidirectionnelle
        if (property.getHostPreferences() != null) {
            property.getHostPreferences().setProperty(property);
        }

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

// PropertyService.java

    public Property partialUpdateProperty(Integer id, Map<String, Object> updates) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found: " + id));

        updates.forEach((key, value) -> {
            switch (key) {
                // Basic Info
                case "title" -> property.setTitle((String) value);
                case "description" -> property.setDescription((String) value);
                case "propertyType" -> property.setPropertyType((String) value);
                case "placeType" -> property.setPlaceType((String) value);
                case "surfaceArea" -> property.setSurfaceArea(value != null ? ((Number) value).doubleValue() : null);
                case "floorNumber" -> property.setFloorNumber(value != null ? ((Number) value).intValue() : null);

                // Capacity
                case "maxGuests" -> property.setMaxGuests(((Number) value).intValue());
                case "bedrooms" -> property.setBedrooms(((Number) value).intValue());
                case "beds" -> property.setBeds(((Number) value).intValue());
                case "bathrooms" -> property.setBathrooms(((Number) value).intValue());

                // Check-in/out
                case "checkInTimeStart" -> property.setCheckInTimeStart((String) value);
                case "checkInTimeEnd" -> property.setCheckInTimeEnd((String) value);
                case "checkOutTime" -> property.setCheckOutTime((String) value);
                case "instantBooking" -> property.setInstantBooking((Boolean) value);

                // Stay Rules
                case "minStayNights" -> property.setMinStayNights(((Number) value).intValue());
                case "maxStayNights" -> property.setMaxStayNights(((Number) value).intValue());
                case "bookingAdvanceDays" -> property.setBookingAdvanceDays(((Number) value).intValue());
                case "cancellationPolicy" -> property.setCancellationPolicy((String) value);

                // Location
                case "adresseLine" -> property.setAdresseLine((String) value);
                case "city" -> property.setCity((String) value);
                case "country" -> property.setCountry((String) value);
                case "postalCode" -> property.setPostalCode((String) value);
                case "latitude" -> property.setLatitude(value != null ? ((Number) value).doubleValue() : null);
                case "longitude" -> property.setLongitude(value != null ? ((Number) value).doubleValue() : null);
                case "neighborhoodDescription" -> property.setNeighborhoodDescription((String) value);

                // Pricing
                case "pricePerNight" -> property.setPricePerNight(((Number) value).doubleValue());
                case "weekendPricePerNight" -> property.setWeekendPricePerNight(((Number) value).doubleValue());

                // Fees
                case "cleaningFee" -> property.setCleaningFee(value != null ? ((Number) value).doubleValue() : null);
                case "petFee" -> property.setPetFee(value != null ? ((Number) value).doubleValue() : null);
                case "platformFeePercentage" -> property.setPlatformFeePercentage(value != null ? ((Number) value).doubleValue() : null);

                // ✅ AJOUT: Property Rules
                case "childrenAllowed", "babiesAllowed", "petsAllowed", "smokingAllowed", "eventsAllowed", "additionalRules" -> {
                    PropertyRule rules = property.getRules();
                    if (rules == null) {
                        rules = new PropertyRule();
                        rules.setProperty(property);
                        property.setRules(rules);
                    }

                    switch (key) {
                        case "childrenAllowed" -> rules.setChildrenAllowed((Boolean) value);
                        case "babiesAllowed" -> rules.setBabiesAllowed((Boolean) value);
                        case "petsAllowed" -> rules.setPetsAllowed((Boolean) value);
                        case "smokingAllowed" -> rules.setSmokingAllowed((Boolean) value);
                        case "eventsAllowed" -> rules.setEventsAllowed((Boolean) value);
                        case "additionalRules" -> rules.setCustomRules((String) value);
                    }
                }

                // ✅ AJOUT: Host Preferences
                case "communicationStyle", "responseTime", "checkInProcess" -> {
                    HostInteractionPreference prefs = property.getHostPreferences();
                    if (prefs == null) {
                        prefs = new HostInteractionPreference();
                        prefs.setProperty(property);
                        property.setHostPreferences(prefs);
                    }

                    switch (key) {
                        case "communicationStyle" -> prefs.setInteractionLevel((String) value);
                        case "responseTime" -> prefs.setCheckInMethod((String) value);
                        case "checkInProcess" -> prefs.setCheckInInstructions((String) value);
                    }
                }
            }
        });

        return propertyRepository.save(property);
    }

    @Transactional
    public Property addAmenityToProperty(Integer propertyId, Integer amenityId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new RuntimeException("Amenity not found: " + amenityId));

        property.getAmenities().add(amenity);

        return propertyRepository.save(property);
    }

    /**
     * Supprimer une amenity d'une property (sans toucher aux autres données)
     */
    @Transactional
    public Property removeAmenityFromProperty(Integer propertyId, Integer amenityId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        property.getAmenities().removeIf(a -> a.getAmenityId().equals(amenityId));

        return propertyRepository.save(property);
    }

    /**
     * Ajouter un discount à une property
     */
    @Transactional
    public Property addDiscountToProperty(Integer propertyId, Discount discountData) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        // Créer le nouveau discount
        Discount discount = new Discount();
        discount.setDiscountType(discountData.getDiscountType());
        discount.setDiscountPercentage(discountData.getDiscountPercentage());
        discount.setMinNights(discountData.getMinNights());
        discount.setDescription(discountData.getDescription());

        // Sauvegarder le discount
        Discount savedDiscount = discountRepository.save(discount);

        // Ajouter à la property
        property.getDiscounts().add(savedDiscount);

        return propertyRepository.save(property);
    }

    /**
     * Supprimer un discount d'une property
     */
    @Transactional
    public Property removeDiscountFromProperty(Integer propertyId, Integer discountId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        property.getDiscounts().removeIf(d -> d.getDiscountId().equals(discountId));

        return propertyRepository.save(property);
    }

    /**
     * Modifier un discount
     */
    @Transactional
    public Discount updateDiscount(Integer discountId, Discount discountData) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + discountId));

        discount.setDiscountType(discountData.getDiscountType());
        discount.setDiscountPercentage(discountData.getDiscountPercentage());
        discount.setMinNights(discountData.getMinNights());
        discount.setDescription(discountData.getDescription());

        return discountRepository.save(discount);
    }




    @Transactional
    public void setPhotoCover(Integer propertyId, Integer photoId) {
        List<PropertyPhoto> photos = photoRepository.findByProperty_PropertyId(propertyId);

        // Trier par displayOrder actuel
        photos.sort((a, b) -> {
            int orderA = a.getDisplayOrder() != null ? a.getDisplayOrder() : 0;
            int orderB = b.getDisplayOrder() != null ? b.getDisplayOrder() : 0;
            return Integer.compare(orderA, orderB);
        });

        // Trouver et retirer la photo cover de la liste
        PropertyPhoto coverPhoto = null;
        for (int i = 0; i < photos.size(); i++) {
            if (photos.get(i).getPhotoId().equals(photoId)) {
                coverPhoto = photos.remove(i);
                break;
            }
        }

        if (coverPhoto == null) {
            throw new RuntimeException("Photo not found: " + photoId);
        }

        // Mettre la cover en premier
        photos.add(0, coverPhoto);

        // Mettre à jour isCover et displayOrder pour toutes les photos
        for (int i = 0; i < photos.size(); i++) {
            PropertyPhoto photo = photos.get(i);
            photo.setIsCover(i == 0);  // Seule la première est cover
            photo.setDisplayOrder(i + 1);  // Ordre de 1 à N
        }

        photoRepository.saveAll(photos);
    }

    /**
     * Réorganiser les photos
     */
    @Transactional
    public void reorderPhotos(Integer propertyId, List<Integer> photoIds) {
        for (int i = 0; i < photoIds.size(); i++) {
            PropertyPhoto photo = photoRepository.findById(photoIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Photo not found"));
            photo.setDisplayOrder(i + 1);
            photoRepository.save(photo);
        }
    }
}