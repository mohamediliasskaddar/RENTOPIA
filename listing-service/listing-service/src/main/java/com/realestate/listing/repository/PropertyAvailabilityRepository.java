package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PropertyAvailabilityRepository extends JpaRepository<PropertyAvailability, Integer> {

    // Toutes les périodes bloquées dans une plage
    List<PropertyAvailability> findByProperty_PropertyIdAndDateDebutBetween(
            Integer propertyId, LocalDate start, LocalDate end);

    // Vérifie conflit (période bloquée chevauche)
    boolean existsByProperty_PropertyIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            Integer propertyId, LocalDate end, LocalDate start);

    // Supprimer une période
    void deleteByProperty_PropertyIdAndDateDebutAndDateFin(
            Integer propertyId, LocalDate dateDebut, LocalDate dateFin);
    // ✅ NOUVELLE MÉTHODE : Trouver par propertyId et because (raison)
    List<PropertyAvailability> findByProperty_PropertyIdAndBecause(Integer propertyId, String because);


    @Query("SELECT a FROM PropertyAvailability a " +
            "WHERE a.property.propertyId = :propertyId " +
            "AND a.isAvailable = false " +
            "AND a.dateDebut <= :end AND a.dateFin >= :start")
    List<PropertyAvailability> findOverlappingBlocks(
            @Param("propertyId") Integer propertyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}