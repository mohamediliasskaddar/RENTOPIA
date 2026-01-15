// src/app/core/models/host-booking.model.ts
import { Booking, ReservationStatus, PriceBreakdown } from './booking.model';
import { UserResponseDTO } from './user.model';

/**
 * Booking enrichi avec infos guest et property pour le Host
 */
export interface HostBooking extends Booking {
  // Infos du guest (locataire)
  guest?: {
    id: number;
    fullName: string;
    email: string;
    photoUrl?: string;
    phone?: string;
  };

  // Infos de la property
  property?: {
    propertyId: number;
    title: string;
    city: string;
    country: string;
    coverPhoto?: string;
  };
}

/**
 * Stats des réservations pour le dashboard host
 */
export interface HostBookingStats {
  total: number;
  pending: number;
  confirmed: number;
  checkedIn: number;
  completed: number;
  cancelled: number;
  totalRevenue: number;
  upcomingRevenue: number;
}
