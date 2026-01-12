package com.rentaldapp.bookingservice.repository;

import com.rentaldapp.bookingservice.model.entity.Reservation;
import com.rentaldapp.bookingservice.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUserId(Integer userId);

    List<Reservation> findByPropertyId(Integer propertyId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByUserIdAndStatus(Integer userId, ReservationStatus status);

    // Vérifier si des dates se chevauchent pour une propriété donnée
    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
            "AND r.status IN ('CONFIRMED', 'CHECKED_IN') " +
            "AND ((r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate))")
    List<Reservation> findOverlappingReservations(
            @Param("propertyId") Integer propertyId,
            @Param("checkInDate") LocalDateTime checkInDate,
            @Param("checkOutDate") LocalDateTime checkOutDate
    );

    // Vérifier si un utilisateur a déjà une réservation pour la même propriété aux mêmes dates
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.userId = :userId " +
            "AND r.propertyId = :propertyId " +
            "AND r.status IN ('CONFIRMED', 'CHECKED_IN') " +
            "AND ((r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate))")
    boolean existsOverlappingReservationForUser(
            @Param("userId") Integer userId,
            @Param("propertyId") Integer propertyId,
            @Param("checkInDate") LocalDateTime checkInDate,
            @Param("checkOutDate") LocalDateTime checkOutDate
    );

    // Réservations à venir pour un utilisateur
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId " +
            "AND r.checkInDate >= CURRENT_DATE " +
            "AND r.status IN ('CONFIRMED', 'CHECKED_IN') " +
            "ORDER BY r.checkInDate ASC")
    List<Reservation> findUpcomingReservationsByUser(@Param("userId") Integer userId);

    // Réservations passées pour un utilisateur
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId " +
            "AND r.checkOutDate < CURRENT_DATE " +
            "AND r.status = 'COMPLETED' " +
            "ORDER BY r.checkOutDate DESC")
    List<Reservation> findPastReservationsByUser(@Param("userId") Integer userId);

    // Réservations nécessitant check-in aujourd'hui
    @Query("SELECT r FROM Reservation r WHERE r.checkInDate = CURRENT_DATE " +
            "AND r.status = 'CONFIRMED'")
    List<Reservation> findReservationsForCheckInToday();

    // Réservations nécessitant check-out aujourd'hui
    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate = CURRENT_DATE " +
            "AND r.status = 'CHECKED_IN'")
    List<Reservation> findReservationsForCheckOutToday();

    /**
     * Récupérer toutes les réservations des propriétés d'un host
     */
    @Query("SELECT r FROM Reservation r WHERE r.propertyId IN :propertyIds ORDER BY r.createdAt DESC")
    List<Reservation> findByPropertyIdIn(@Param("propertyIds") List<Integer> propertyIds);
}