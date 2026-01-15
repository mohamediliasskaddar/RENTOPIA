// src/app/core/models/booking-with-snapshot.model.ts

import { ReservationStatus } from './booking.model';

/**
 * Booking enrichi avec les snapshots de la property
 * Utilisé pour afficher les réservations avec les données de la property
 * telles qu'elles étaient au moment de la réservation
 */
export interface BookingWithSnapshot {
  // Booking info
  bookingId: number;
  propertyId: number;
  userId: number;
  hostId: number;
  checkIn: string;
  checkOut: string;
  totalPrice: number;
  status: ReservationStatus;
  createdAt: string;

  // ✅ CORRIGÉ: Ajouter userCanReview
  hasReview?: boolean;         // L'utilisateur a-t-il déjà révisé cette réservation?
  reviewId?: number;           // ID de l'avis existant
  userCanReview?: boolean;     // ⚠️ AJOUTER CETTE PROPRIÉTÉ
  // Property snapshot (comme elle était au moment de la réservation)
  propertySnapshot: PropertySnapshotData;

  // Host info (actuel)
  hostName?: string;
  hostPhoto?: string;

  review?: {
    id: number;
    ratingValue: number;
    reviewText: string;
    createdAt: string;
    isVisible: boolean;
  };
}

export interface PropertySnapshotData {
  // General info
  title: string;
  description: string;
  propertyType: string;
  placeType: string;
  city: string;
  country: string;
  address: string;

  // Caractéristiques
  maxGuests: number;
  bedrooms: number;
  beds: number;
  bathrooms: number;
  surfaceArea?: number;
  floorNumber?: number;

  // Prix (au moment de la réservation)
  pricePerNight: number;
  weekendPricePerNight?: number;
  cleaningFee?: number;
  petFee?: number;

  // Check-in/out
  checkInTimeStart?: string;
  checkInTimeEnd?: string;
  checkOutTime?: string;

  // Photos (au moment de la réservation)
  photos: PropertyPhotoSnapshot[];

  // Amenities (au moment de la réservation)
  amenities: PropertyAmenitySnapshot[];

  // Rules (au moment de la réservation)
  rules: PropertyRulesSnapshot;
}

export interface PropertyPhotoSnapshot {
  photoUrl: string;
  isCover: boolean;
  displayOrder: number;
}

export interface PropertyAmenitySnapshot {
  amenityId: number;
  name: string;
  category: string;
  icon?: string;
}

export interface PropertyRulesSnapshot {
  childrenAllowed: boolean;
  babiesAllowed: boolean;
  petsAllowed: boolean;
  smokingAllowed: boolean;
  eventsAllowed: boolean;
  customRules?: string;
}

/**
 * Filtres pour les bookings
 */
export interface BookingFilters {
  status?: ReservationStatus;
  upcoming?: boolean;
  past?: boolean;
}

/**
 * Stats des bookings
 */
export interface BookingStats {
  totalBookings: number;
  upcomingBookings: number;
  pastBookings: number;
  cancelledBookings: number;
  totalSpent: number;
}
