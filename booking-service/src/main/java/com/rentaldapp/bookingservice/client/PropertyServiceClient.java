package com.rentaldapp.bookingservice.client;

import com.rentaldapp.bookingservice.model.dto.PropertyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Client Feign pour communiquer avec le Listing Service (Property Service)
 * ✅ CORRECTION : Utilise maintenant services.listing-service.url
 */
@FeignClient(name = "listing-service", url = "${services.listing-service.url:http://localhost:8082}")
public interface PropertyServiceClient {

    /**
     * Récupérer les détails d'une propriété
     */
    @GetMapping("/properties/{id}")
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
     * ✅ CORRECTION : POST au lieu de GET pour bloquer les dates
     */
    @PostMapping("/properties/{id}/availability/block")
    void blockDates(
            @PathVariable("id") Integer propertyId,
            @RequestParam("checkIn") LocalDateTime checkIn,
            @RequestParam("checkOut") LocalDateTime checkOut,
            @RequestParam("reservationId") Integer reservationId
    );

    /**
     * ✅ CORRECTION : POST au lieu de GET pour débloquer les dates
     */
    @PostMapping("/properties/{id}/availability/unblock")
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

    // PropertyServiceClient.java

    @GetMapping("/properties/my")
    List<PropertyDTO> getPropertiesByUserId(@RequestParam("userId") Integer userId);
}