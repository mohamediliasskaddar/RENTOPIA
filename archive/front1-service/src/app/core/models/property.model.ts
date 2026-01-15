// src/app/core/models/property.model.ts

import { Amenity } from './amenity.model';
import { Discount } from './discount.model';

/**
 * ============================
 * ENUM PropertyStatus
 * ============================
 */
export enum PropertyStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED',
  DELETED = 'DELETED'
}

/**
 * ============================
 * Interface Property (Entité complète)
 * Correspond EXACTEMENT à l'entité Property backend
 * ============================
 */
export interface Property {
  propertyId: number;
  userId: number;
  title: string;
  description: string;
  propertyType: string;
  placeType: string;
  adresseLine: string;
  city: string;
  country: string;
  postalCode: string;
  latitude?: number;
  longitude?: number;
  neighborhoodDescription?: string;
  floorNumber?: number;
  surfaceArea?: number;
  maxGuests: number;
  bedrooms: number;
  beds: number;
  bathrooms: number;
  weekendPricePerNight: number;
  pricePerNight: number;
  cleaningFee?: number;
  petFee?: number;
  platformFeePercentage?: number;
  minStayNights?: number;
  maxStayNights?: number;
  bookingAdvanceDays?: number;
  checkInTimeStart?: string;
  checkInTimeEnd?: string;
  checkOutTime?: string;
  instantBooking: boolean;
  cancellationPolicy?: string;
  status: PropertyStatus;
  blockchainPropertyId?: string;
  blockchainTxHash?: string;
  createdAt: string;
  updatedAt: string;

  // Relations (sans @JsonIgnore côté frontend)
  amenities?: Amenity[];
  photos?: PropertyPhoto[];
  availabilities?: PropertyAvailability[];
  discounts?: Discount[];
  rules?: PropertyRule;
  priceHistory?: PriceHistory[];
  hostPreferences?: HostInteractionPreference;
}

/**
 * ============================
 * Interface PropertySearchResultDTO
 * Correspond EXACTEMENT au PropertySearchResultDTO backend
 * Retourné par /api/properties/search/tenant
 * ============================
 */
export interface PropertySearchResultDTO {
  propertyId: number;
  title: string;
  city: string;
  country: string;
  averagePricePerNight: number;  // Prix moyen par nuit
  totalPrice: number;             // Prix total calculé
  discountPercentage: number;     // % de réduction appliquée
  discountType: string;           // Type de réduction
  checkIn: string;                // Date ISO
  checkOut: string;               // Date ISO
  nights: number;
  maxGuests: number;
  bedrooms: number;
  beds: number;
  bathrooms: number;
}

/**
 * ============================
 * Interface PropertyPhoto
 * ============================
 */
export interface PropertyPhoto {
  photoId: number;
  photoUrl: string;
  isCover: boolean;
  displayOrder: number;
  photoHash?: string;
}

/**
 * ============================
 * Interface PropertyRule
 * ============================
 */
export interface PropertyRule {
  ruleId: number;
  childrenAllowed: boolean;
  babiesAllowed: boolean;
  petsAllowed: boolean;
  smokingAllowed: boolean;
  eventsAllowed: boolean;
  customRules?: string;
}

/**
 * ============================
 * Interface PropertyAvailability
 * ============================
 */
export interface PropertyAvailability {
  availabilityId: number;
  dateDebut: string;  // LocalDate → string ISO
  dateFin: string;
  isAvailable: boolean;
  because?: string;   // "owner_block" | "booked"
}

/**
 * ============================
 * Interface PriceHistory
 * ============================
 */
export interface PriceHistory {
  historyId: number;
  oldPrice: number;
  newPrice: number;
  priceType: string;
  changedAt: string;
}

/**
 * ============================
 * Interface HostInteractionPreference
 * ============================
 */
export interface HostInteractionPreference {
  preferenceId: number;
  interactionLevel: string;
  checkInMethod: string;
  checkInInstructions?: string;
}

/**
 * ============================
 * Interface PropertyVersion
 * ============================
 */
export interface PropertyVersion {
  versionId: number;
  propertyId: number;
  createdAt: string;
  numVersion: number;
}

/**
 * ============================
 * Interface pour les filtres de recherche
 * ============================
 */
export interface PropertySearchFilters {
  city?: string;
  country?: string;
  checkIn?: string;
  checkOut?: string;
  adults: number;
  children?: number;
  babies?: number;
  pets?: number;
  propertyType?: string;
  placeType?: string;
  minPrice?: number;
  maxPrice?: number;
  bedrooms?: number;
  bathrooms?: number;
  beds?: number;
  instantBooking?: boolean;
  amenityIds?: number[];
  smokingAllowed?: boolean;
  eventsAllowed?: boolean;
  isFirstBooking?: boolean;
  bookingDate?: string;
}

/**
 * ============================
 * Interface pour créer/mettre à jour une property
 * ============================
 */
export interface CreatePropertyDTO {
  userId: number;
  title: string;
  description: string;
  propertyType: string;
  placeType: string;
  adresseLine: string;
  city: string;
  country: string;
  postalCode: string;
  latitude?: number;
  longitude?: number;
  maxGuests: number;
  bedrooms: number;
  beds: number;
  bathrooms: number;
  PricePerNight: number;
  weekendPricePerNight: number;
  instantBooking: boolean;
}

/**
 * ============================
 * Types de propriétés disponibles
 * ============================
 */
export const PROPERTY_TYPES = [
  { value: 'house', label: 'Maison' },
  { value: 'apartment', label: 'Appartement' },
  { value: 'villa', label: 'Villa' },
  { value: 'studio', label: 'Studio' },
  { value: 'loft', label: 'Loft' }
] as const;

/**
 * ============================
 * Types de logement
 * ============================
 */
export const PLACE_TYPES = [
  { value: 'entire_place', label: 'Logement entier' },
  { value: 'private_room', label: 'Chambre privée' },
  { value: 'shared_room', label: 'Chambre partagée' }
] as const;
