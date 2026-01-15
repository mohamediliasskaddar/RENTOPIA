// src/app/core/models/property-card.model.ts

/**
 * DTO léger pour afficher les properties en liste (cards)
 * Correspond à PropertyCardDTO backend
 */
export interface PropertyCard {
  propertyId: number;
  title: string;
  city: string;
  country: string;
  propertyType: string;
  placeType: string;

  maxGuests: number;
  bedrooms: number;
  beds: number;
  bathrooms: number;

  pricePerNight: number;
  weekendPricePerNight?: number;

  // Photo principale
  mainPhotoUrl?: string;

  // Optionnel (pour plus tard)
  averageRating?: number;
  reviewCount?: number;
  status?: string;
}
