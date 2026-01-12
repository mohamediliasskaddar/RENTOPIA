// src/main/java/com/realestate/listing/controller/PropertyController.java
package com.realestate.listing.controller;

import com.realestate.listing.dto.PropertyCardDTO;
import com.realestate.listing.dto.PropertyDetailDTO;
import com.realestate.listing.dto.PropertySearchResultDTO;
import com.realestate.listing.entity.Amenity;
import com.realestate.listing.entity.Discount;
import com.realestate.listing.entity.PriceHistory;
import com.realestate.listing.entity.Property;
import com.realestate.listing.entity.Property.PropertyStatus;
import com.realestate.listing.mapper.PropertyCardMapper;
import com.realestate.listing.mapper.PropertyDetailMapper;
import com.realestate.listing.repository.DiscountRepository;
import com.realestate.listing.service.AmenityService;
import com.realestate.listing.service.PriceHistoryService;
import com.realestate.listing.service.PropertyAvailabilityService;
import com.realestate.listing.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyAvailabilityService availabilityService;

    @Autowired
    private PriceHistoryService priceHistoryService;

    @Autowired
    private PropertyDetailMapper propertyDetailMapper;

    @Autowired
    private PropertyCardMapper propertyCardMapper;

    @Autowired
    private AmenityService amenityService;


    @Autowired
    private DiscountRepository discountRepository;


    /**
     * ✅ Vérifier la disponibilité d'une propriété
     * Endpoint: GET /api/properties/{id}/availability/check
     */
    @GetMapping("/{id}/availability/check")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut
    ) {
        boolean isAvailable = availabilityService.checkAvailability(id, checkIn, checkOut);
        return ResponseEntity.ok(isAvailable);
    }


    /**
     * ✅ NOUVEAU : Vérifier si un utilisateur est propriétaire
     * Endpoint: GET /api/properties/{id}/owner/{userId}
     */
    @GetMapping("/{id}/owner/{userId}")
    public ResponseEntity<Boolean> isOwner(
            @PathVariable Integer id,
            @PathVariable Integer userId
    ) {
        boolean isOwner = propertyService.isOwner(id, userId);
        return ResponseEntity.ok(isOwner);
    }


    // === CRUD ===
    @GetMapping("/all")
    public Page<PropertyCardDTO> getAll(Pageable pageable) {
        Page<Property> properties = propertyService.getAllProperties(pageable);
        return properties.map(propertyCardMapper::toCardDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getById(@PathVariable Integer id) {
        return propertyService.getPropertyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/new")
    public ResponseEntity<Property> create(@RequestBody Property property) {
        try {
            return ResponseEntity.ok(propertyService.createProperty(property));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> update(@PathVariable Integer id, @RequestBody Property property) {
        return ResponseEntity.ok(propertyService.updateProperty(id, property));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public List<PropertyCardDTO> getMyProperties(@RequestParam Integer userId) {
        return propertyService.getAllByUserId(userId).stream()
                .map(propertyCardMapper::toCardDTO)
                .toList();
    }

    @GetMapping("/my/status")
    public List<Property> getMyPropertiesByStatus(
            @RequestParam Integer userId,
            @RequestParam PropertyStatus status) {
        return propertyService.getByUserIdAndStatus(userId, status);
    }

    /**
     * ========================================
     * NOUVEAU ENDPOINT : FILTRAGE SIMPLE (SANS DATES)
     * GET /properties/filter
     * <p>
     * Utilisé quand on filtre directement depuis /listings
     * SANS recherche préalable (pas de dates)
     * ========================================
     */
    @GetMapping("/filter")
    public List<Property> filterProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer adults,
            @RequestParam(defaultValue = "0") Integer children,
            @RequestParam(defaultValue = "0") Integer babies,
            @RequestParam(defaultValue = "0") Integer pets,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) String placeType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Integer bathrooms,
            @RequestParam(required = false) Integer beds,
            @RequestParam(required = false) Boolean instantBooking,
            @RequestParam(required = false) List<Integer> amenityIds,
            @RequestParam(required = false) Boolean smokingAllowed,
            @RequestParam(required = false) Boolean eventsAllowed) {

        int totalGuests = (adults != null ? adults : 1) + children + babies;

        // Recherche avec les filtres (sans vérification de disponibilité)
        return propertyService.searchProperties(
                city, country, propertyType, placeType,
                minPrice, maxPrice, bedrooms, bathrooms, beds, totalGuests,
                instantBooking, amenityIds,
                children > 0 ? true : null,
                babies > 0 ? true : null,
                pets > 0 ? true : null,
                smokingAllowed,
                eventsAllowed
        );
    }

    /**
     * ========================================
     * ENDPOINT EXISTANT : RECHERCHE AVEC DATES
     * GET /properties/search/tenant
     * <p>
     * Utilisé quand on recherche depuis la search bar
     * AVEC dates (checkIn/checkOut REQUIRED)
     * ========================================
     */
    @GetMapping("/search/tenant")
    public List<PropertySearchResultDTO> searchForTenant(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam Integer adults,
            @RequestParam(defaultValue = "0") Integer children,
            @RequestParam(defaultValue = "0") Integer babies,
            @RequestParam(defaultValue = "0") Integer pets,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) String placeType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Integer bathrooms,
            @RequestParam(required = false) Integer beds,
            @RequestParam(required = false) Boolean instantBooking,
            @RequestParam(required = false) List<Integer> amenityIds,
            @RequestParam(required = false) Boolean smokingAllowed,
            @RequestParam(required = false) Boolean eventsAllowed,
            @RequestParam(defaultValue = "false") boolean isFirstBooking,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate) {

        LocalDate today = LocalDate.now();
        LocalDate finalBookingDate = bookingDate != null ? bookingDate : today;
        int totalGuests = adults + children + babies;
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        // === 1. Filtres de base ===
        List<Property> candidates = propertyService.searchProperties(
                city, country, propertyType, placeType,
                minPrice, maxPrice, bedrooms, bathrooms, beds, totalGuests,
                instantBooking, amenityIds,
                children > 0 ? true : null,
                babies > 0 ? true : null,
                pets > 0 ? true : null,
                smokingAllowed,
                eventsAllowed
        );

        // === 2. Disponibilité ===
        List<Property> available = candidates.stream()
                .filter(p -> !availabilityService.hasConflict(p.getPropertyId(), checkIn, checkOut.minusDays(1)))
                .toList();

        // === 3. Calcul du prix + DTO ===
        return available.stream()
                .map(p -> {
                    double total = propertyService.calculateTotalPrice(p, checkIn, checkOut, isFirstBooking, finalBookingDate);
                    double avgPerNight = total / nights;

                    // Trouver la réduction appliquée
                    double bestDiscount = 0.0;
                    String discountType = "Aucune";
                    for (Discount d : p.getDiscounts()) {
                        if (d.getMinNights() != null && nights < d.getMinNights()) continue;
                        boolean applies = switch (d.getDiscountType()) {
                            case "first_booking" -> isFirstBooking;
                            case "last_minute" -> ChronoUnit.DAYS.between(finalBookingDate, checkIn) <= 14;
                            case "weekly", "monthly" -> true;
                            default -> false;
                        };
                        if (applies && d.getDiscountPercentage() > bestDiscount) {
                            bestDiscount = d.getDiscountPercentage();
                            discountType = d.getDiscountType();
                        }
                    }

                    return new PropertySearchResultDTO(
                            p.getPropertyId(),
                            p.getTitle(),
                            p.getCity(),
                            p.getCountry(),
                            avgPerNight,
                            total,
                            bestDiscount,
                            discountType,
                            checkIn,
                            checkOut,
                            (int) nights
                    );
                })
                .toList();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<Property> publish(@PathVariable Integer id) {
        return ResponseEntity.ok(propertyService.publishProperty(id));
    }

    @GetMapping("/{id}/price-history")
    public List<PriceHistory> getPriceHistory(@PathVariable Integer id) {
        return priceHistoryService.getByPropertyId(id);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<PropertyDetailDTO> getPropertyDetails(@PathVariable Integer id) {
        return propertyService.getPropertyById(id)
                .map(property -> {
                    PropertyDetailDTO dto = propertyDetailMapper.toDTO(property);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH - Mise à jour partielle d'une property
     * Ne modifie que les champs envoyés, garde les autres intacts
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Property> partialUpdate(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates
    ) {
        return ResponseEntity.ok(propertyService.partialUpdateProperty(id, updates));

    }


    @PostMapping("/{propertyId}/amenities/{amenityId}")
    public ResponseEntity<Property> addAmenityToProperty(
            @PathVariable Integer propertyId,
            @PathVariable Integer amenityId
    ) {
        Property updated = propertyService.addAmenityToProperty(propertyId, amenityId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprimer une amenity d'une property
     */
    @DeleteMapping("/{propertyId}/amenities/{amenityId}")
    public ResponseEntity<Property> removeAmenityFromProperty(
            @PathVariable Integer propertyId,
            @PathVariable Integer amenityId
    ) {
        Property updated = propertyService.removeAmenityFromProperty(propertyId, amenityId);
        return ResponseEntity.ok(updated);
    }


    /**
     * Ajouter un discount à une property
     */
    @PostMapping("/{propertyId}/discounts")
    public ResponseEntity<Property> addDiscountToProperty(
            @PathVariable Integer propertyId,
            @RequestBody Discount discount
    ) {
        Property updated = propertyService.addDiscountToProperty(propertyId, discount);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprimer un discount d'une property
     */
    @DeleteMapping("/{propertyId}/discounts/{discountId}")
    public ResponseEntity<Property> removeDiscountFromProperty(
            @PathVariable Integer propertyId,
            @PathVariable Integer discountId
    ) {
        Property updated = propertyService.removeDiscountFromProperty(propertyId, discountId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Modifier un discount
     */
    @PutMapping("/{propertyId}/discounts/{discountId}")
    public ResponseEntity<Discount> updateDiscount(
            @PathVariable Integer propertyId,
            @PathVariable Integer discountId,
            @RequestBody Discount discountData
    ) {
        Discount updated = propertyService.updateDiscount(discountId, discountData);
        return ResponseEntity.ok(updated);
    }

    /**
     * Définir une photo comme couverture
     */
    @PutMapping("/{propertyId}/photos/{photoId}/cover")
    public ResponseEntity<Void> setPhotoCover(
            @PathVariable Integer propertyId,
            @PathVariable Integer photoId
    ) {
        propertyService.setPhotoCover(propertyId, photoId);
        return ResponseEntity.ok().build();
    }

    /**
     * Réorganiser les photos
     */
    @PutMapping("/{propertyId}/photos/reorder")
    public ResponseEntity<Void> reorderPhotos(
            @PathVariable Integer propertyId,
            @RequestBody Map<String, List<Integer>> request
    ) {
        List<Integer> photoIds = request.get("photoIds");
        propertyService.reorderPhotos(propertyId, photoIds);
        return ResponseEntity.ok().build();
    }
}