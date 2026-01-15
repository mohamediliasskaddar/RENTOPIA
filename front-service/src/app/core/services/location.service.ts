// src/app/core/services/location.service.ts

import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ApiService } from './api.service';
import { environment } from '../../../environments/environment';

/**
 * ============================
 * Interface Location
 * ============================
 */
export interface Location {
  city: string;
  country: string;
  displayName: string; // "City, Country"
}

/**
 * ============================
 * SERVICE LOCATION
 * Gère les suggestions de villes et pays pour l'autocomplete
 * ============================
 */
@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private readonly baseUrl = environment.services.listing;
  // Base de données simulée des locations populaires
  // Utilisée comme FALLBACK si l'API backend est indisponible
  private readonly popularLocations: Location[] = [
    // Maroc
    { city: 'Rabat', country: 'Morocco', displayName: 'Rabat, Morocco' },
    { city: 'Casablanca', country: 'Morocco', displayName: 'Casablanca, Morocco' },
    { city: 'Marrakech', country: 'Morocco', displayName: 'Marrakech, Morocco' },
    { city: 'Fes', country: 'Morocco', displayName: 'Fes, Morocco' },
    { city: 'Tangier', country: 'Morocco', displayName: 'Tangier, Morocco' },
    { city: 'Agadir', country: 'Morocco', displayName: 'Agadir, Morocco' },
    { city: 'Essaouira', country: 'Morocco', displayName: 'Essaouira, Morocco' },

    // France
    { city: 'Paris', country: 'France', displayName: 'Paris, France' },
    { city: 'Lyon', country: 'France', displayName: 'Lyon, France' },
    { city: 'Marseille', country: 'France', displayName: 'Marseille, France' },
    { city: 'Nice', country: 'France', displayName: 'Nice, France' },
    { city: 'Bordeaux', country: 'France', displayName: 'Bordeaux, France' },

    // Espagne
    { city: 'Barcelona', country: 'Spain', displayName: 'Barcelona, Spain' },
    { city: 'Madrid', country: 'Spain', displayName: 'Madrid, Spain' },
    { city: 'Seville', country: 'Spain', displayName: 'Seville, Spain' },
    { city: 'Valencia', country: 'Spain', displayName: 'Valencia, Spain' },

    // Italie
    { city: 'Rome', country: 'Italy', displayName: 'Rome, Italy' },
    { city: 'Milan', country: 'Italy', displayName: 'Milan, Italy' },
    { city: 'Venice', country: 'Italy', displayName: 'Venice, Italy' },
    { city: 'Florence', country: 'Italy', displayName: 'Florence, Italy' },

    // États-Unis
    { city: 'New York', country: 'United States', displayName: 'New York, United States' },
    { city: 'Los Angeles', country: 'United States', displayName: 'Los Angeles, United States' },
    { city: 'San Francisco', country: 'United States', displayName: 'San Francisco, United States' },
    { city: 'Miami', country: 'United States', displayName: 'Miami, United States' },

    // Royaume-Uni
    { city: 'London', country: 'United Kingdom', displayName: 'London, United Kingdom' },
    { city: 'Manchester', country: 'United Kingdom', displayName: 'Manchester, United Kingdom' },
    { city: 'Edinburgh', country: 'United Kingdom', displayName: 'Edinburgh, United Kingdom' },

    // Émirats Arabes Unis
    { city: 'Dubai', country: 'United Arab Emirates', displayName: 'Dubai, United Arab Emirates' },
    { city: 'Abu Dhabi', country: 'United Arab Emirates', displayName: 'Abu Dhabi, United Arab Emirates' },

    // Turquie
    { city: 'Istanbul', country: 'Turkey', displayName: 'Istanbul, Turkey' },
    { city: 'Ankara', country: 'Turkey', displayName: 'Ankara, Turkey' },

    // Grèce
    { city: 'Athens', country: 'Greece', displayName: 'Athens, Greece' },
    { city: 'Santorini', country: 'Greece', displayName: 'Santorini, Greece' },
  ];

  constructor(private apiService: ApiService) {}

  /**
   * ============================
   * RECHERCHER DES LOCATIONS (BACKEND)
   * Retourne les locations correspondant au terme de recherche
   * ============================
   */
  searchLocations(query: string): Observable<Location[]> {
    // Appel API au backend
    return this.apiService.get<Location[]>(`${this.baseUrl}/locations/search`, {
      q: query || '',
      fromDatabase: false // true pour chercher dans la BDD, false pour liste statique
    }).pipe(
      map(locations => locations || []),
      catchError(error => {
        console.error('Erreur lors de la recherche de locations:', error);
        // Fallback : retourner la liste locale si l'API échoue
        return of(this.searchLocationsLocally(query));
      })
    );
  }

  /**
   * ============================
   * RECHERCHE LOCALE (FALLBACK)
   * Utilisée si l'API backend est indisponible
   * ============================
   */
  private searchLocationsLocally(query: string): Location[] {
    if (!query || query.trim().length < 2) {
      return this.popularLocations.slice(0, 8);
    }

    const searchTerm = query.toLowerCase().trim();

    const filtered = this.popularLocations.filter(location =>
      location.city.toLowerCase().includes(searchTerm) ||
      location.country.toLowerCase().includes(searchTerm) ||
      location.displayName.toLowerCase().includes(searchTerm)
    );

    const sorted = filtered.sort((a, b) => {
      const aStartsWithCity = a.city.toLowerCase().startsWith(searchTerm);
      const bStartsWithCity = b.city.toLowerCase().startsWith(searchTerm);
      const aStartsWithCountry = a.country.toLowerCase().startsWith(searchTerm);
      const bStartsWithCountry = b.country.toLowerCase().startsWith(searchTerm);

      if (aStartsWithCity && !bStartsWithCity) return -1;
      if (!aStartsWithCity && bStartsWithCity) return 1;
      if (aStartsWithCountry && !bStartsWithCountry) return -1;
      if (!aStartsWithCountry && bStartsWithCountry) return 1;

      return a.city.localeCompare(b.city);
    });

    return sorted.slice(0, 10);
  }

  /**
   * ============================
   * OBTENIR LES LOCATIONS POPULAIRES (BACKEND)
   * ============================
   */
  getPopularLocations(limit: number = 8): Observable<Location[]> {
    return this.apiService.get<Location[]>(`${this.baseUrl}/listings/locations/popular`, { limit }).pipe(
      map(locations => locations || []),
      catchError(error => {
        console.error('Erreur lors de la récupération des locations populaires:', error);
        // Fallback : retourner la liste locale
        return of(this.popularLocations.slice(0, limit));
      })
    );
  }

  /**
   * ============================
   * PARSER UNE LOCATION DEPUIS displayName
   * Exemple: "Rabat, Morocco" → { city: "Rabat", country: "Morocco" }
   * ============================
   */
  parseLocationString(displayName: string): { city: string; country: string } | null {
    const parts = displayName.split(',').map(s => s.trim());
    if (parts.length === 2) {
      return {
        city: parts[0],
        country: parts[1]
      };
    }
    return null;
  }

  /**
   * ============================
   * VERSION FUTURE: Appel API backend
   * TODO: Implémenter quand le backend aura un endpoint /locations/search
   * ============================
   */
  /*
  searchLocationsFromAPI(query: string): Observable<Location[]> {
    return this.apiService.get<Location[]>(`/locations/search?q=${query}`);
  }
  */
}
