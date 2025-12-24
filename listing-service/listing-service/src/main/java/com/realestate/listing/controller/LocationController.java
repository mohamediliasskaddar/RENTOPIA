package com.realestate.listing.controller;


import com.realestate.listing.dto.LocationDTO;
import com.realestate.listing.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================
 * CONTROLLER LOCATION
 * Endpoints pour l'autocomplete des villes/pays
 * ============================
 */
@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    /**
     * ============================
     * RECHERCHER DES LOCATIONS
     * GET /api/listings/locations/search?q={query}
     *
     * Exemples :
     * - /api/listings/locations/search?q=Raba     → Rabat, Morocco
     * - /api/listings/locations/search?q=Morocco  → Toutes les villes du Maroc
     * - /api/listings/locations/search?q=         → 8 locations populaires
     * ============================
     */
    @GetMapping("/search")
    public ResponseEntity<List<LocationDTO>> searchLocations(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "false") boolean fromDatabase
    ) {
        log.info("📍 GET /api/listings/locations/search?q={}&fromDatabase={}", q, fromDatabase);

        List<LocationDTO> locations;

        if (fromDatabase) {
            // Recherche dans la base de données (locations réelles avec properties)
            locations = locationService.searchLocationsFromDatabase(q);
        } else {
            // Recherche dans la liste statique (plus rapide)
            locations = locationService.searchLocations(q);
        }

        log.info("✅ {} locations retournées", locations.size());
        return ResponseEntity.ok(locations);
    }

    /**
     * ============================
     * OBTENIR LES LOCATIONS POPULAIRES
     * GET /api/listings/locations/popular?limit={limit}
     *
     * Retourne les N locations les plus populaires
     * ============================
     */
    @GetMapping("/popular")
    public ResponseEntity<List<LocationDTO>> getPopularLocations(
            @RequestParam(required = false, defaultValue = "8") int limit
    ) {
        log.info("📍 GET /api/listings/locations/popular?limit={}", limit);

        List<LocationDTO> locations = locationService.getPopularLocations(limit);

        log.info("✅ {} locations populaires retournées", locations.size());
        return ResponseEntity.ok(locations);
    }

    /**
     * ============================
     * OBTENIR TOUTES LES LOCATIONS DEPUIS LA BDD
     * GET /api/listings/locations/all
     *
     * Retourne UNIQUEMENT les villes où il y a des properties ACTIVE
     * Trié par nombre de properties (DESC)
     * ============================
     */
    @GetMapping("/all")
    public ResponseEntity<List<LocationDTO>> getAllLocationsFromDatabase() {
        log.info("📍 GET /api/listings/locations/all");

        List<LocationDTO> locations = locationService.getLocationsFromDatabase();

        log.info("✅ {} locations retournées depuis la BDD", locations.size());
        return ResponseEntity.ok(locations);
    }
}
