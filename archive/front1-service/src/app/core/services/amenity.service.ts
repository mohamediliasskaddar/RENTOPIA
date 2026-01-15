// src/app/core/services/amenity.service.ts

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { environment } from '../../../environments/environment';
import { Amenity } from '../models/amenity.model';

/**
 * ============================
 * SERVICE AMENITY
 * Gère les équipements (WiFi, Piscine, etc.)
 * ============================
 */
@Injectable({
  providedIn: 'root'
})
export class AmenityService {
  private readonly baseUrl = environment.services.listing; // '/listings'

  constructor(private apiService: ApiService) {}

  /**
   * ============================
   * RÉCUPÉRER TOUTES LES AMENITIES
   * GET /listings/amenities
   * ============================
   */
  getAllAmenities(): Observable<Amenity[]> {
    return this.apiService.get<Amenity[]>(`${this.baseUrl}/amenities/all`);
  }

  /**
   * ============================
   * RÉCUPÉRER PAR CATÉGORIE
   * GET /listings/amenities/category/{category}
   * ============================
   */
  getByCategory(category: string): Observable<Amenity[]> {
    return this.apiService.get<Amenity[]>(`${this.baseUrl}/amenities/category/${category}`);
  }

  /**
   * ============================
   * RECHERCHER PAR NOM
   * GET /listings/amenities/search?name={name}
   * ============================
   */
  searchByName(name: string): Observable<Amenity[]> {
    return this.apiService.get<Amenity[]>(`${this.baseUrl}/amenities/search`, { name });
  }
}
