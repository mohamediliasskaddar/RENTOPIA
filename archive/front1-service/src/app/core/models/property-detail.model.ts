// src/app/core/models/property-detail.model.ts

export interface PropertyDetail {
  // Informations de base
  propertyId: number;
  userId: number;
  title: string;
  description: string;
  propertyType: string;
  placeType: string;

  // Localisation
  adresseLine: string;
  city: string;
  country: string;
  postalCode: string;
  latitude: number;
  longitude: number;
  neighborhoodDescription?: string;

  // Caractéristiques
  floorNumber?: number;
  surfaceArea?: number;
  maxGuests: number;
  bedrooms: number;
  beds: number;
  bathrooms: number;

  // Prix
  pricePerNight: number;
  weekendPricePerNight?: number;
  cleaningFee?: number;
  petFee?: number;
  platformFeePercentage?: number;

  // Règles de réservation
  minStayNights?: number;
  maxStayNights?: number;
  bookingAdvanceDays?: number;
  checkInTimeStart?: string;
  checkInTimeEnd?: string;
  checkOutTime?: string;
  instantBooking?: boolean;
  cancellationPolicy?: string;

  // Status
  status: string;

  // Blockchain
  blockchainPropertyId?: string;
  blockchainTxHash?: string;

  // Dates
  createdAt: string;
  updatedAt: string;

  // Relations
  amenities: PropertyAmenity[];
  photos: PropertyPhoto[];
  discounts: PropertyDiscount[];
  rules?: PropertyRules;
  hostPreferences?: HostPreferences;
}

export interface PropertyAmenity {
  amenityId: number;
  name: string;
  icon?: string;
  category?: string;
}

export interface PropertyPhoto {
  photoId: number;
  photoUrl: string;
  photoHash?: string;
  isCover: boolean;
  displayOrder: number;
}

export interface PropertyDiscount {
  discountId: number;
  discountType: string;
  discountPercentage: number;
  minNights?: number;
}

export interface PropertyRules {
  childrenAllowed: boolean;
  babiesAllowed: boolean;
  petsAllowed: boolean;
  smokingAllowed: boolean;
  eventsAllowed: boolean;
  additionalRules?: string;
}

export interface HostPreferences {
  communicationStyle?: string;
  responseTime?: string;
  checkInProcess?: string;
}
