package com.rentaldapp.bookingservice.client;

import com.rentaldapp.bookingservice.model.dto.PropertyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Client Feign pour communiquer avec le Property Service
 *
 * @FeignClient:
 * - name: Nom logique du service dans Eureka
 * - path: Chemin de base de l'API
 * - url: URL directe (optionnel, pour bypass Eureka en dev)
 */
@FeignClient(name = "listing-service", url = "${services.user.url:http://localhost:8082}")
public interface PropertyServiceClient {

    /**
     * Récupérer les détails d'une propriété
     */
    @GetMapping("/api/properties/{id}")
    PropertyDTO getPropertyById(@PathVariable("id") Integer id);

    /**
     * Vérifier la disponibilité d'une propriété pour des dates données
     */
    @GetMapping("/properties/{id}/availability/check")
    Boolean checkAvailability(
            @PathVariable("id") Integer propertyId,
            @RequestParam("checkIn") LocalDateTime checkIn,
            @RequestParam("checkOut") LocalDateTime checkOut
    );

    /**
     * Récupérer le prix d'une propriété
     */
    @GetMapping("/properties/{id}/pricing")
    PropertyDTO getPropertyPricing(@PathVariable("id") Integer propertyId);

    /**
     * Bloquer les dates d'une propriété (après confirmation de réservation)
     */
    @GetMapping("/properties/{id}/availability/block")
    void blockDates(
            @PathVariable("id") Integer propertyId,
            @RequestParam("checkIn") LocalDateTime checkIn,
            @RequestParam("checkOut") LocalDateTime checkOut,
            @RequestParam("reservationId") Integer reservationId
    );

    /**
     * Débloquer les dates (en cas d'annulation)
     */
    @GetMapping("/properties/{id}/availability/unblock")
    void unblockDates(
            @PathVariable("id") Integer propertyId,
            @RequestParam("reservationId") Integer reservationId
    );

    /**
     * Vérifier que l'utilisateur est le propriétaire
     */
    @GetMapping("/properties/{id}/owner/{userId}")
    Boolean isOwner(
            @PathVariable("id") Integer propertyId,
            @PathVariable("userId") Integer userId
    );
}