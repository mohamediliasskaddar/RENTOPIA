package com.realestate.listing.repository;

import com.realestate.listing.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Integer> {

    List<Property> findByUserId(Integer userId);
    List<Property> findByUserIdAndStatus(Integer userId, Property.PropertyStatus status);
    Optional<Property> findByPropertyIdAndUserId(Integer propertyId, Integer userId);

    List<Property> findByStatus(Property.PropertyStatus status);
    Page<Property> findByStatus(Property.PropertyStatus status, Pageable pageable);

    /**
     * ========================================
     * RECHERCHE AVANCÉE AVEC FILTRES
     * ✅ CORRECTION : Suppression de r.propertyRuleId du GROUP BY
     * ========================================
     */
    @Query("""
    SELECT DISTINCT p FROM Property p 
    LEFT JOIN p.amenities a 
    LEFT JOIN p.rules r 
    WHERE p.status = 'ACTIVE'
      AND (:city IS NULL OR LOWER(p.city) = LOWER(:city))
      AND (:country IS NULL OR LOWER(p.country) = LOWER(:country))
      AND (:propertyType IS NULL OR LOWER(p.propertyType) = LOWER(:propertyType))
      AND (:placeType IS NULL OR LOWER(p.placeType) = LOWER(:placeType))
      AND (:minPrice IS NULL OR p.PricePerNight >= :minPrice)
      AND (:maxPrice IS NULL OR p.PricePerNight <= :maxPrice)
      AND (:bedrooms IS NULL OR p.bedrooms >= :bedrooms)
      AND (:bathrooms IS NULL OR p.bathrooms >= :bathrooms)
      AND (:beds IS NULL OR p.beds >= :beds)
      AND (:maxGuests IS NULL OR p.maxGuests >= :maxGuests)
      AND (:instantBooking IS NULL OR p.instantBooking = :instantBooking)
      AND (:childrenAllowed IS NULL OR r.childrenAllowed = :childrenAllowed)
      AND (:babiesAllowed IS NULL OR r.babiesAllowed = :babiesAllowed)
      AND (:petsAllowed IS NULL OR r.petsAllowed = :petsAllowed)
      AND (:smokingAllowed IS NULL OR r.smokingAllowed = :smokingAllowed)
      AND (:eventsAllowed IS NULL OR r.eventsAllowed = :eventsAllowed)
    GROUP BY p.propertyId, p.userId, p.title, p.description, p.propertyType, p.placeType,
             p.adresseLine, p.city, p.country, p.postalCode, p.latitude, p.longitude,
             p.neighborhoodDescription, p.floorNumber, p.surfaceArea, p.maxGuests,
             p.bedrooms, p.beds, p.bathrooms, p.weekendPricePerNight, p.PricePerNight,
             p.cleaningFee, p.petFee, p.platformFeePercentage, p.minStayNights, p.maxStayNights,
             p.bookingAdvanceDays, p.checkInTimeStart, p.checkInTimeEnd, p.checkOutTime,
             p.instantBooking, p.cancellationPolicy, p.status, p.blockchainPropertyId,
             p.blockchainTxHash, p.createdAt, p.updatedAt
    HAVING :amenityIds IS NULL 
       OR COUNT(DISTINCT CASE WHEN a.amenityId IN :amenityIds THEN a.amenityId END) = :amenityCount
    """)
    List<Property> searchProperties(
            @Param("city") String city,
            @Param("country") String country,
            @Param("propertyType") String propertyType,
            @Param("placeType") String placeType,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("bedrooms") Integer bedrooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("beds") Integer beds,
            @Param("maxGuests") Integer maxGuests,
            @Param("instantBooking") Boolean instantBooking,
            @Param("amenityIds") List<Integer> amenityIds,
            @Param("amenityCount") Integer amenityCount,
            @Param("childrenAllowed") Boolean childrenAllowed,
            @Param("babiesAllowed") Boolean babiesAllowed,
            @Param("petsAllowed") Boolean petsAllowed,
            @Param("smokingAllowed") Boolean smokingAllowed,
            @Param("eventsAllowed") Boolean eventsAllowed
    );

    boolean existsByTitleAndCityAndAdresseLine(String title, String city, String adresseLine);

    @Query("""
        SELECT p.city, p.country, COUNT(p) 
        FROM Property p 
        WHERE p.status = 'ACTIVE' 
        GROUP BY p.city, p.country 
        ORDER BY COUNT(p) DESC
    """)
    List<Object[]> findDistinctCitiesAndCountries();
}