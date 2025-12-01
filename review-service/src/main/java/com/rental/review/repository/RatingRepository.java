package com.rental.review.repository;

import com.rental.review.entity.Rating;
import com.rental.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Trouver une note par review
    Optional<Rating> findByReview(Review review);

    // Trouver une note par reservation ID
    Optional<Rating> findByReservationId(Integer reservationId);

    // Calculer la note moyenne pour une propriété (utilise une requête native)
    @Query(value = "SELECT AVG(rat.rating_value) FROM ratings rat " +
            "JOIN reviews rev ON rat.reservation_id = rev.reservation_id " +
            "WHERE rev.property_id = :propertyId AND rev.is_visible = 1",
            nativeQuery = true)
    Double calculateAverageRatingForProperty(@Param("propertyId") Integer propertyId);

    // Trouver toutes les notes pour une propriété
    List<Rating> findByPropertyId(Integer propertyId);

    // Compter les notes par valeur pour une propriété
    @Query(value = "SELECT COUNT(*) FROM ratings rat " +
            "JOIN reviews rev ON rat.reservation_id = rev.reservation_id " +
            "WHERE rev.property_id = :propertyId " +
            "AND rev.is_visible = 1 " +
            "AND rat.rating_value >= :minValue AND rat.rating_value < :maxValue",
            nativeQuery = true)
    Long countByPropertyIdAndRatingRange(
            @Param("propertyId") Integer propertyId,
            @Param("minValue") Double minValue,
            @Param("maxValue") Double maxValue
    );
}