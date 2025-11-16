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
public interface PropertyRepository extends JpaRepository<Property, Integer> { // JpaRepository donne toutes les méthodes CRUD

    // query methods : JPA génère automatiquement la requête à partir du nom de la méthode
    List<Property> findByUserId(Integer userId);
    List<Property> findByUserIdAndStatus(Integer userId, Property.PropertyStatus status);
    Optional<Property> findByPropertyIdAndUserId(Integer propertyId, Integer userId);// // Optional sert à éviter les valeurs null en renvoyant soit la propriété trouvée, soit un résultat vide.


    List<Property> findByStatus(Property.PropertyStatus status); // "active", "archived"
    Page<Property> findByStatus(Property.PropertyStatus status, Pageable pageable);

    // === RECHERCHE AVANCÉE ===
    // Cette méthode permet de rechercher des propriétés avec plusieurs filtres optionnels.
    // Elle exécute la requête JPQL définie dans l'annotation @Query
    // Dans PropertyRepository.java

    @Query("""
    SELECT DISTINCT p FROM Property p 
    LEFT JOIN p.amenities a 
    LEFT JOIN p.rules r 
    WHERE p.status = 'ACTIVE'
      AND (:city IS NULL OR LOWER(p.city) = LOWER(:city))
      AND (:country IS NULL OR LOWER(p.country) = LOWER(:country))
      AND (:propertyType IS NULL OR p.propertyType = :propertyType)
      AND (:placeType IS NULL OR p.placeType = :placeType)
      AND (:minPrice IS NULL OR p.PricePerNight >= :minPrice)
      AND (:maxPrice IS NULL OR p.PricePerNight <= :maxPrice)
      AND (:bedrooms IS NULL OR p.bedrooms >= :bedrooms)
      AND (:bathrooms IS NULL OR p.bathrooms >= :bathrooms)
      AND (:beds IS NULL OR p.beds >= :beds)
      AND (:maxGuests IS NULL OR p.maxGuests >= :maxGuests)
      AND (:instantBooking IS NULL OR p.instantBooking = :instantBooking)
      AND (:amenityIds IS NULL OR a.amenityId IN :amenityIds)
      AND (:childrenAllowed IS NULL OR r.childrenAllowed = :childrenAllowed)
      AND (:babiesAllowed IS NULL OR r.babiesAllowed = :babiesAllowed)
      AND (:petsAllowed IS NULL OR r.petsAllowed = :petsAllowed)
      AND (:smokingAllowed IS NULL OR r.smokingAllowed = :smokingAllowed)
      AND (:eventsAllowed IS NULL OR r.eventsAllowed = :eventsAllowed)
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
            @Param("childrenAllowed") Boolean childrenAllowed,
            @Param("babiesAllowed") Boolean babiesAllowed,
            @Param("petsAllowed") Boolean petsAllowed,
            @Param("smokingAllowed") Boolean smokingAllowed,
            @Param("eventsAllowed") Boolean eventsAllowed
    );


    // === PRÉVENTION DU DOUBLON ===
    boolean existsByTitleAndCityAndAdresseLine(String title, String city, String adresseLine);
}