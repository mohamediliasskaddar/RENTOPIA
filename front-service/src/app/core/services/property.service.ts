// src/app/core/services/property.service.ts

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { environment } from '../../../environments/environment';
import {
  Property,
  PropertySearchResultDTO,
  PropertySearchFilters,
  CreatePropertyDTO,
  PriceHistory, PropertyAvailability
} from '../models/property.model';
import {PropertyDetail} from "../models/property-detail.model";
import {PropertyCard} from "../models/property-card.model";
import {Discount} from "../models/discount.model";

import { PricePredictionRequest, PricePredictionResponse } from '../models/price-prediction.model';
/**
 * ============================
 * SERVICE PROPERTY
 * Gère toutes les opérations sur les properties
 * ============================
 */
@Injectable({
  providedIn: 'root'
})


export class PropertyService {
  private readonly baseUrl = environment.services.listing; // '/listings'

  constructor(private apiService: ApiService) {}

  /**
   * ============================
   * RÉCUPÉRER TOUTES LES PROPERTIES (ACTIVE)
   * GET /listings/properties/all
   * ============================
   */
  getAllProperties(page: number = 0, size: number = 20): Observable<Page<PropertyCard>> {
    return this.apiService.get<Page<PropertyCard>>(`${this.baseUrl}/properties/all`, {
      page,
      size
    });
  }
  getBlockedDates(
    propertyId: number,
    start: string,
    end: string
  ): Observable<PropertyAvailability[]> {
    return this.apiService.get<PropertyAvailability[]>(
      `${this.baseUrl}/properties/${propertyId}/availability/blocked`,
      { start, end }
    );
  }

  /**
   * ============================
   * VÉRIFIER CONFLIT DE DATES
   * GET /listings/properties/{id}/availability/conflict
   * ============================
   */
  checkDateConflict(
    propertyId: number,
    start: string,
    end: string
  ): Observable<boolean> {
    return this.apiService.get<boolean>(
      `${this.baseUrl}/properties/${propertyId}/availability/conflict`,
      { start, end }
    );
  }

  /**
   * ============================
   * RÉCUPÉRER UNE PROPERTY PAR ID
   * GET /listings/properties/{id}
   * ============================
   */
  getPropertyById(id: number): Observable<Property> {
    return this.apiService.get<Property>(`${this.baseUrl}/properties/${id}`);
  }

  /**
   * ============================
   * FILTRER LES PROPERTIES (SANS DATES)
   * GET /listings/properties/filter
   *
   * Utilisé quand on applique des filtres SANS avoir fait de recherche
   * (pas de dates checkIn/checkOut)
   * Retourne des Property[] normales
   * ============================
   */
  /**
   * ============================
   * FILTRER LES PROPERTIES (SANS DATES)
   * GET /listings/properties/filter
   *
   * Retourne PropertyCard[] (avec mainPhotoUrl)
   * ============================
   */
  filterProperties(filters: PropertySearchFilters): Observable<PropertyCard[]> {
    const params: any = {
      adults: filters.adults || 1,
      children: filters.children || 0,
      babies: filters.babies || 0,
      pets: filters.pets || 0
    };

    if (filters.city) params.city = filters.city;
    if (filters.country) params.country = filters.country;
    if (filters.propertyType) params.propertyType = filters.propertyType;
    if (filters.placeType) params.placeType = filters.placeType;
    if (filters.minPrice !== undefined) params.minPrice = filters.minPrice;
    if (filters.maxPrice !== undefined) params.maxPrice = filters.maxPrice;
    if (filters.bedrooms) params.bedrooms = filters.bedrooms;
    if (filters.bathrooms) params.bathrooms = filters.bathrooms;
    if (filters.beds) params.beds = filters.beds;
    if (filters.instantBooking !== undefined) params.instantBooking = filters.instantBooking;
    if (filters.amenityIds && filters.amenityIds.length > 0) {
      params.amenityIds = filters.amenityIds;
    }
    if (filters.smokingAllowed !== undefined) params.smokingAllowed = filters.smokingAllowed;
    if (filters.eventsAllowed !== undefined) params.eventsAllowed = filters.eventsAllowed;

    return this.apiService.get<PropertyCard[]>(
      `${this.baseUrl}/properties/filter`,
      params
    );
  }



  /**
   * ============================
   * RECHERCHE AVANCÉE (AVEC DATES)
   * GET /listings/properties/search/tenant
   *
   * Utilisé quand on recherche depuis la search bar
   * (avec dates checkIn/checkOut REQUIRED)
   * Retourne PropertySearchResultDTO[] avec prix calculés
   * ============================
   */
  searchProperties(filters: PropertySearchFilters): Observable<PropertySearchResultDTO[]> {
    // Construire les query params
    const params: any = {
      checkIn: filters.checkIn,  // ✅ REQUIRED
      checkOut: filters.checkOut, // ✅ REQUIRED
      adults: filters.adults,
      children: filters.children || 0,
      babies: filters.babies || 0,
      pets: filters.pets || 0
    };

    // Ajouter les filtres optionnels
    if (filters.city) params.city = filters.city;
    if (filters.country) params.country = filters.country;
    if (filters.propertyType) params.propertyType = filters.propertyType;
    if (filters.placeType) params.placeType = filters.placeType;
    if (filters.minPrice !== undefined) params.minPrice = filters.minPrice;
    if (filters.maxPrice !== undefined) params.maxPrice = filters.maxPrice;
    if (filters.bedrooms) params.bedrooms = filters.bedrooms;
    if (filters.bathrooms) params.bathrooms = filters.bathrooms;
    if (filters.beds) params.beds = filters.beds;
    if (filters.instantBooking !== undefined) params.instantBooking = filters.instantBooking;

    // ✅ Même correction pour searchProperties
    if (filters.amenityIds && filters.amenityIds.length > 0) {
      params.amenityIds = filters.amenityIds;
    }

    if (filters.smokingAllowed !== undefined) params.smokingAllowed = filters.smokingAllowed;
    if (filters.eventsAllowed !== undefined) params.eventsAllowed = filters.eventsAllowed;
    if (filters.isFirstBooking !== undefined) params.isFirstBooking = filters.isFirstBooking;
    if (filters.bookingDate) params.bookingDate = filters.bookingDate;

    return this.apiService.get<PropertySearchResultDTO[]>(
      `${this.baseUrl}/properties/search/tenant`,
      params
    );
  }

  /**
   * ============================
   * CRÉER UNE PROPERTY
   * POST /listings/properties
   * ============================
   */
  createProperty(property: CreatePropertyDTO): Observable<Property> {
    return this.apiService.post<Property>(`${this.baseUrl}/properties/new`, property);
  }

  /**
   * ============================
   * METTRE À JOUR UNE PROPERTY
   * PUT /listings/properties/{id}
   * ============================
   */
  updateProperty(id: number, property: Partial<Property>): Observable<Property> {
    return this.apiService.put<Property>(`${this.baseUrl}/properties/${id}`, property);
  }

  /**
   * ============================
   * PUBLIER UNE PROPERTY (DRAFT → ACTIVE)
   * PATCH /listings/properties/{id}/publish
   * ============================
   */
  publishProperty(id: number): Observable<Property> {
    return this.apiService.patch<Property>(`${this.baseUrl}/properties/${id}/publish`);
  }

  /**
   * ============================
   * SUPPRIMER UNE PROPERTY
   * DELETE /listings/properties/{id}
   * ============================
   */
  deleteProperty(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.baseUrl}/properties/${id}`);
  }

  /**
   * ============================
   * MES PROPERTIES (HÔTE)
   * GET /listings/properties/my?userId={id}
   * ============================
   */
  getMyProperties(userId: number): Observable<PropertyCard[]> {
    return this.apiService.get<PropertyCard[]>(`${this.baseUrl}/properties/my`, { userId });
  }

  /**
   * ============================
   * HISTORIQUE DES PRIX
   * GET /listings/properties/{id}/price-history
   * ============================
   */
  getPriceHistory(propertyId: number): Observable<PriceHistory[]> {
    return this.apiService.get<PriceHistory[]>(
      `${this.baseUrl}/properties/${propertyId}/price-history`
    );
  }

  getPropertyDetails(id: number): Observable<PropertyDetail> {
    return this.apiService.get<PropertyDetail>(`${this.baseUrl}/properties/${id}/details`);
  }

  /**
   * ============================
   * MISE À JOUR PARTIELLE (PATCH)
   * PATCH /listings/properties/{id}
   * ============================
   */
  patchProperty(id: number, updates: Partial<Property>): Observable<Property> {
    return this.apiService.patch<Property>(`${this.baseUrl}/properties/${id}`, updates);
  }

  /**
   * ============================
   * BLOQUER DES DATES
   * POST /listings/properties/{id}/availability/block
   * ============================
   */
  blockDates(propertyId: number, start: string, end: string, reason: string): Observable<void> {
    return this.apiService.post<void>(
      `${this.baseUrl}/properties/${propertyId}/availability/block`,
      { start, end, reason }
    );
  }

  /**
   * ============================
   * DÉBLOQUER DES DATES
   * DELETE /listings/properties/{id}/availability/unblock
   * ============================
   */

  unblockDates(propertyId: number, start: string, end: string): Observable<void> {
    return this.apiService.post<void>(  // ✅ Changer delete en post
      `${this.baseUrl}/properties/${propertyId}/availability/unblock`,
      { start, end, reason: '' }  // ✅ Envoyer un body JSON
    );
  }

  /**
   * ============================
   * RÉCUPÉRER TOUTES LES DISPONIBILITÉS
   * GET /listings/properties/{id}/availability/blocked
   * ============================
   */
  getAvailabilities(propertyId: number, start: string, end: string): Observable<PropertyAvailability[]> {
    return this.apiService.get<PropertyAvailability[]>(
      `${this.baseUrl}/properties/${propertyId}/availability/blocked`,
      { start, end }
    );
  }


  // src/app/core/services/property.service.ts
// ✅ AJOUTER CES MÉTHODES

  /**
   * ============================
   * AJOUTER UNE AMENITY À UNE PROPERTY
   * POST /listings/properties/{id}/amenities/{amenityId}
   * ============================
   */
  addAmenityToProperty(propertyId: number, amenityId: number): Observable<void> {
    return this.apiService.post<void>(
      `${this.baseUrl}/properties/${propertyId}/amenities/${amenityId}`,
      {}
    );
  }

  /**
   * ============================
   * SUPPRIMER UNE AMENITY D'UNE PROPERTY
   * DELETE /listings/properties/{id}/amenities/{amenityId}
   * ============================
   */
  removeAmenityFromProperty(propertyId: number, amenityId: number): Observable<void> {
    return this.apiService.delete<void>(
      `${this.baseUrl}/properties/${propertyId}/amenities/${amenityId}`
    );
  }

  /**
   * ============================
   * AJOUTER UN DISCOUNT À UNE PROPERTY
   * POST /listings/properties/{id}/discounts
   * ============================
   */
  addDiscountToProperty(propertyId: number, discount: Partial<Discount>): Observable<void> {
    return this.apiService.post<void>(
      `${this.baseUrl}/properties/${propertyId}/discounts`,
      discount
    );
  }

  /**
   * ============================
   * SUPPRIMER UN DISCOUNT D'UNE PROPERTY
   * DELETE /listings/properties/{id}/discounts/{discountId}
   * ============================
   */
  removeDiscountFromProperty(propertyId: number, discountId: number): Observable<void> {
    return this.apiService.delete<void>(
      `${this.baseUrl}/properties/${propertyId}/discounts/${discountId}`
    );
  }

  /**
   * ============================
   * MODIFIER UN DISCOUNT
   * PUT /listings/properties/{id}/discounts/{discountId}
   * ============================
   */
  updateDiscount(propertyId: number, discountId: number, discount: Partial<Discount>): Observable<void> {
    return this.apiService.put<void>(
      `${this.baseUrl}/properties/${propertyId}/discounts/${discountId}`,
      discount
    );
  }



  /**
   * Définir une photo comme couverture
   * PUT /listings/properties/{propertyId}/photos/{photoId}/cover
   */
  setPhotoCover(propertyId: number, photoId: number): Observable<void> {
    return this.apiService.put<void>(
      `${this.baseUrl}/properties/${propertyId}/photos/${photoId}/cover`,
      {}
    );
  }

  /**
   * Mettre à jour l'ordre des photos
   * PUT /listings/properties/{propertyId}/photos/reorder
   */
  reorderPhotos(propertyId: number, photoIds: number[]): Observable<void> {
    return this.apiService.put<void>(
      `${this.baseUrl}/properties/${propertyId}/photos/reorder`,
      { photoIds }
    );
  }


  /**
   * ============================
   * PRÉDICTION DE PRIX (NOUVELLE PROPRIÉTÉ)
   * POST /listings/properties/suggest-price
   *
   * Utilisé dans le formulaire de création
   * ============================
   */
  suggestPriceForNewProperty(request: PricePredictionRequest): Observable<PricePredictionResponse> {
    return this.apiService.post<PricePredictionResponse>(
      `${this.baseUrl}/properties/suggest-price`,
      request
    );
  }

  /**
   * ============================
   * PRÉDICTION DE PRIX (PROPRIÉTÉ EXISTANTE)
   * GET /listings/properties/{id}/suggest-price
   *
   * Utilisé pour ajuster le prix d'une propriété existante
   * ============================
   */
  suggestPriceForExistingProperty(propertyId: number): Observable<PricePredictionResponse> {
    return this.apiService.get<PricePredictionResponse>(
      `${this.baseUrl}/properties/${propertyId}/suggest-price`
    );
  }

}
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
