package com.rental.review.repository;

import com.rental.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Trouver un avis par reservation ID
    Optional<Review> findByReservationId(Integer reservationId);

    // Vérifier si un avis existe pour une réservation
    boolean existsByReservationId(Integer reservationId);

    // Trouver tous les avis d'une propriété
    List<Review> findByPropertyIdAndIsVisibleTrueOrderByCreatedAtDesc(Integer propertyId);

    // Trouver tous les avis donnés par un utilisateur (via une table de référence externe)
    // Nous devons utiliser une approche différente puisque Reservation n'est pas dans ce module
    @Query(value = "SELECT r.* FROM reviews r " +
            "WHERE EXISTS (SELECT 1 FROM reservations res WHERE res.id = r.reservation_id AND res.user_id = :userId) " +
            "ORDER BY r.created_at DESC", nativeQuery = true)
    List<Review> findByUserId(@Param("userId") Integer userId);

    // Trouver les avis visibles
    List<Review> findByIsVisibleTrue();

    // Trouver les avis récents (dernières 24h par exemple)
    List<Review> findByCreatedAtAfter(LocalDateTime dateTime);

    // Compter le nombre d'avis pour une propriété
    Long countByPropertyIdAndIsVisibleTrue(Integer propertyId);

    // Supprimer les avis invisibles plus anciens qu'une certaine date
    void deleteByIsVisibleFalseAndCreatedAtBefore(LocalDateTime dateTime);
}