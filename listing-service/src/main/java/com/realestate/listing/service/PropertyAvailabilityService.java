// src/main/java/com/realestate/listing/service/PropertyAvailabilityService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.Property;
import com.realestate.listing.entity.PropertyAvailability;
import com.realestate.listing.repository.PropertyAvailabilityRepository;
import com.realestate.listing.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
@Service
@Transactional
public class PropertyAvailabilityService {

    @Autowired private PropertyAvailabilityRepository repo;
    @Autowired private PropertyRepository propertyRepo;

    public void blockPeriod(Integer propertyId, LocalDate start, LocalDate end, String reason) {
        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (hasConflict(propertyId, start, end)) {
            throw new IllegalArgumentException("Période déjà bloquée");
        }

        PropertyAvailability block = PropertyAvailability.builder()
                .property(property)
                .dateDebut(start)
                .dateFin(end)
                .isAvailable(false)
                .because(reason)
                .build();

        repo.save(block);
    }
    /*
    public void unblockPeriod(Integer propertyId, LocalDate start, LocalDate end) {
        repo.deleteByProperty_PropertyIdAndDateDebutAndDateFin(propertyId, start, end);
    }
*/

    @Transactional
    public void unblockPeriod(Integer propertyId, LocalDate unblockStart, LocalDate unblockEnd) {
        if (unblockStart.isAfter(unblockEnd)) {
            throw new IllegalArgumentException("Date de début doit être avant la fin");
        }

        // Trouver TOUS les blocs qui chevauchent
        List<PropertyAvailability> overlapping = repo.findOverlappingBlocks(
                propertyId, unblockStart, unblockEnd);

        if (overlapping.isEmpty()) {
            throw new IllegalArgumentException("Aucune période bloquée dans cette plage");
        }

        for (PropertyAvailability block : overlapping) {
            LocalDate blockStart = block.getDateDebut();
            LocalDate blockEnd = block.getDateFin();

            // Supprimer le bloc d'origine
            repo.delete(block);

            // === PARTIE GAUCHE : avant le déblocage ===
            if (blockStart.isBefore(unblockStart)) {
                PropertyAvailability left = PropertyAvailability.builder()
                        .property(block.getProperty())
                        .dateDebut(blockStart)
                        .dateFin(unblockStart.minusDays(1))
                        .isAvailable(false)
                        .because(block.getBecause())
                        .build();
                repo.save(left);
            }

            // === PARTIE DROITE : après le déblocage ===
            if (blockEnd.isAfter(unblockEnd)) {
                PropertyAvailability right = PropertyAvailability.builder()
                        .property(block.getProperty())
                        .dateDebut(unblockEnd.plusDays(1))
                        .dateFin(blockEnd)
                        .isAvailable(false)
                        .because(block.getBecause())
                        .build();
                repo.save(right);
            }

            // Si déblocage total → rien à faire
        }
    }

    public boolean hasConflict(Integer propertyId, LocalDate start, LocalDate end) {
        return repo.existsByProperty_PropertyIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                propertyId, end, start);
    }

    public List<PropertyAvailability> getBlockedPeriods(Integer propertyId, LocalDate start, LocalDate end) {
        return repo.findByProperty_PropertyIdAndDateDebutBetween(propertyId, start, end);
    }
    // ========================================================================
    // ✅ NOUVELLES MÉTHODES POUR BOOKING-SERVICE (avec LocalDateTime)
    // ========================================================================

    /**
     * ✅ NOUVELLE : Vérifier disponibilité avec LocalDateTime
     * Appelée depuis PropertyController pour Booking Service
     */
    public boolean checkAvailability(Integer propertyId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // Convertir LocalDateTime en LocalDate
        LocalDate startDate = checkIn.toLocalDate();
        LocalDate endDate = checkOut.toLocalDate();

        // Vérifier que la propriété existe
        if (!propertyRepo.existsById(propertyId)) {
            throw new IllegalArgumentException("Propriété non trouvée");
        }

        // Utiliser la méthode existante hasConflict
        return !hasConflict(propertyId, startDate, endDate);
    }

    /**
     * ✅ NOUVELLE : Bloquer des dates pour une réservation avec LocalDateTime
     * Appelée depuis PropertyController pour Booking Service
     */
    public void blockDates(Integer propertyId, LocalDateTime checkIn, LocalDateTime checkOut, Integer reservationId) {
        LocalDate startDate = checkIn.toLocalDate();
        LocalDate endDate = checkOut.toLocalDate();

        // Utiliser la méthode existante blockPeriod avec un reason spécifique
        String reason = "Réservation #" + reservationId;
        blockPeriod(propertyId, startDate, endDate, reason);
    }

    /**
     * ✅ NOUVELLE : Débloquer des dates pour une réservation
     * Appelée depuis PropertyController pour Booking Service
     */
    public void unblockDates(Integer propertyId, Integer reservationId) {
        // Trouver toutes les périodes bloquées pour cette réservation
        String reason = "Réservation #" + reservationId;

        List<PropertyAvailability> blocks = repo.findByProperty_PropertyIdAndBecause(propertyId, reason);

        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("Aucune période bloquée trouvée pour cette réservation");
        }

        // Supprimer tous les blocs liés à cette réservation
        repo.deleteAll(blocks);
    }

}