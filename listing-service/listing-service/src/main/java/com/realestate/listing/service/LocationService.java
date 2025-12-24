package com.realestate.listing.service;


import com.realestate.listing.dto.LocationDTO;
import com.realestate.listing.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================
 * SERVICE LOCATION
 * Gère les locations (villes/pays) pour l'autocomplete
 * ============================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final PropertyRepository propertyRepository;

    // Liste des locations populaires (peut être stockée en base de données plus tard)
    private static final List<LocationDTO> POPULAR_LOCATIONS = Arrays.asList(
            // Maroc
            new LocationDTO("Rabat", "Morocco"),
            new LocationDTO("Casablanca", "Morocco"),
            new LocationDTO("Marrakech", "Morocco"),
            new LocationDTO("Fes", "Morocco"),
            new LocationDTO("Tangier", "Morocco"),
            new LocationDTO("Agadir", "Morocco"),
            new LocationDTO("Essaouira", "Morocco"),

            // France
            new LocationDTO("Paris", "France"),
            new LocationDTO("Lyon", "France"),
            new LocationDTO("Marseille", "France"),
            new LocationDTO("Nice", "France"),
            new LocationDTO("Bordeaux", "France"),

            // Espagne
            new LocationDTO("Barcelona", "Spain"),
            new LocationDTO("Madrid", "Spain"),
            new LocationDTO("Seville", "Spain"),
            new LocationDTO("Valencia", "Spain"),

            // Italie
            new LocationDTO("Rome", "Italy"),
            new LocationDTO("Milan", "Italy"),
            new LocationDTO("Venice", "Italy"),
            new LocationDTO("Florence", "Italy"),

            // États-Unis
            new LocationDTO("New York", "United States"),
            new LocationDTO("Los Angeles", "United States"),
            new LocationDTO("San Francisco", "United States"),
            new LocationDTO("Miami", "United States"),

            // Royaume-Uni
            new LocationDTO("London", "United Kingdom"),
            new LocationDTO("Manchester", "United Kingdom"),
            new LocationDTO("Edinburgh", "United Kingdom"),

            // Émirats Arabes Unis
            new LocationDTO("Dubai", "United Arab Emirates"),
            new LocationDTO("Abu Dhabi", "United Arab Emirates"),

            // Turquie
            new LocationDTO("Istanbul", "Turkey"),
            new LocationDTO("Ankara", "Turkey"),

            // Grèce
            new LocationDTO("Athens", "Greece"),
            new LocationDTO("Santorini", "Greece")
    );

    /**
     * ============================
     * RECHERCHER DES LOCATIONS
     * Retourne les locations correspondant au terme de recherche
     *
     * @param query Terme de recherche (ville ou pays)
     * @return Liste des locations filtrées et triées par pertinence
     * ============================
     */
    public List<LocationDTO> searchLocations(String query) {
        log.info("🔍 Recherche de locations pour : {}", query);

        // Si la recherche est vide ou trop courte, retourner les locations populaires
        if (query == null || query.trim().length() < 2) {
            log.info("✅ Retour des 8 locations populaires");
            return getPopularLocations(8);
        }

        String searchTerm = query.toLowerCase().trim();

        // Filtrer les locations qui correspondent à la recherche
        List<LocationDTO> filtered = POPULAR_LOCATIONS.stream()
                .filter(location ->
                        location.getCity().toLowerCase().contains(searchTerm) ||
                                location.getCountry().toLowerCase().contains(searchTerm) ||
                                location.getDisplayName().toLowerCase().contains(searchTerm)
                )
                .collect(Collectors.toList());

        // Trier par pertinence (correspondances exactes en premier)
        List<LocationDTO> sorted = filtered.stream()
                .sorted((a, b) -> {
                    boolean aStartsWithCity = a.getCity().toLowerCase().startsWith(searchTerm);
                    boolean bStartsWithCity = b.getCity().toLowerCase().startsWith(searchTerm);
                    boolean aStartsWithCountry = a.getCountry().toLowerCase().startsWith(searchTerm);
                    boolean bStartsWithCountry = b.getCountry().toLowerCase().startsWith(searchTerm);

                    if (aStartsWithCity && !bStartsWithCity) return -1;
                    if (!aStartsWithCity && bStartsWithCity) return 1;
                    if (aStartsWithCountry && !bStartsWithCountry) return -1;
                    if (!aStartsWithCountry && bStartsWithCountry) return 1;

                    return a.getCity().compareTo(b.getCity());
                })
                .limit(10) // Limiter à 10 résultats
                .collect(Collectors.toList());

        log.info("✅ {} locations trouvées pour '{}'", sorted.size(), query);
        return sorted;
    }

    /**
     * ============================
     * OBTENIR LES LOCATIONS POPULAIRES
     *
     * @param limit Nombre de locations à retourner
     * @return Liste des locations populaires
     * ============================
     */
    public List<LocationDTO> getPopularLocations(int limit) {
        return POPULAR_LOCATIONS.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * ============================
     * OBTENIR LES LOCATIONS DEPUIS LA BASE DE DONNÉES
     * Récupère les villes/pays UNIQUES depuis les properties existantes
     *
     * Cette méthode est MEILLEURE car elle retourne uniquement les locations
     * où il y a des properties disponibles
     * ============================
     */
    public List<LocationDTO> getLocationsFromDatabase() {
        log.info("🔍 Récupération des locations depuis la base de données");

        // Requête native pour obtenir les city/country uniques avec compteur
        List<Object[]> results = propertyRepository.findDistinctCitiesAndCountries();

        List<LocationDTO> locations = results.stream()
                .map(row -> new LocationDTO(
                        (String) row[0],  // city
                        (String) row[1] // country
                ))

                .collect(Collectors.toList());

        log.info("✅ {} locations trouvées dans la base de données", locations.size());
        return locations;
    }

    /**
     * ============================
     * RECHERCHER DANS LA BASE DE DONNÉES
     * Recherche des locations RÉELLES (avec properties disponibles)
     *
     * @param query Terme de recherche
     * @return Liste des locations filtrées depuis la BDD
     * ============================
     */
    public List<LocationDTO> searchLocationsFromDatabase(String query) {
        log.info("🔍 Recherche de locations dans la BDD pour : {}", query);

        List<LocationDTO> allLocations = getLocationsFromDatabase();

        if (query == null || query.trim().length() < 2) {
            return allLocations.stream().limit(8).collect(Collectors.toList());
        }

        String searchTerm = query.toLowerCase().trim();

        return allLocations.stream()
                .filter(location ->
                        location.getCity().toLowerCase().contains(searchTerm) ||
                                location.getCountry().toLowerCase().contains(searchTerm)
                )
                .limit(10)
                .collect(Collectors.toList());
    }
}