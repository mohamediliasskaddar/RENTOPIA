// src/app/core/models/booking.model.ts

/**
 * ============================
 * ENUM ReservationStatus
 * Correspond EXACTEMENT au backend BookingService
 * ============================
 */
export enum ReservationStatus {
  PENDING = 'PENDING',           // En attente de paiement
  CONFIRMED = 'CONFIRMED',       // Confirmée et payée
  CHECKED_IN = 'CHECKED_IN',     // Client arrivé
  COMPLETED = 'COMPLETED',       // Séjour terminé
  CANCELLED = 'CANCELLED',       // Annulée
  REFUNDED = 'REFUNDED'         // Remboursée
}

/**
 * ============================
 * Interface PriceBreakdown
 * Correspond au PriceBreakdownDTO backend
 * ============================
 */
export interface PriceBreakdown {
  lockedPricePerNight: number;      // Prix par nuit au moment de la réservation
  baseAmount: number;                // Montant de base (nuits × prix)
  discountAmount: number;            // Réduction appliquée
  cleaningFee: number;               // Frais de ménage
  petFee: number;                    // Frais pour animaux
  serviceFee: number;                // Frais de service (10%)
  totalAmount: number;               // Montant total à payer
  platformFeePercentage: number;     // Pourcentage de frais plateforme (5%)
}

/**
 * ============================
 * Interface Booking (Reservation)
 * Correspond EXACTEMENT au ReservationResponseDTO backend
 * ============================
 */
export interface Booking {
  id: number;
  propertyId: number;
  versionId?: number;                // Version de la propriété au moment de la réservation
  userId: number;                    // ID du locataire
  checkInDate: string;               // Format ISO: "2025-01-15T14:00:00"
  checkOutDate: string;              // Format ISO: "2025-01-20T11:00:00"
  totalNights: number;
  numGuests: number;
  status: ReservationStatus;
  cancelledAt?: string;              // Date d'annulation (si applicable)
  createdAt: string;

  // Prix
  priceBreakdown: PriceBreakdown;

  // Blockchain
  blockchainTxHash?: string;         // Hash de la transaction de paiement
  escrowReleased: boolean;           // Fonds libérés au propriétaire
  escrowReleaseTxHash?: string;      // Hash de libération escrow
}

/**
 * ============================
 * DTO CreateBookingDTO
 * Correspond au CreateBookingDTO backend
 * Pour créer une nouvelle réservation
 * ============================
 */
export interface CreateBookingDTO {
  propertyId: number;
  versionId?: number;                // Optionnel, calculé automatiquement si null
  checkInDate: string;               // Format ISO
  checkOutDate: string;              // Format ISO
  numGuests: number;
  hasPets: boolean;
  specialRequests?: string;
}

/**
 * ============================
 * Interface BookingFilters
 * Pour filtrer les réservations (upcoming, past, etc.)
 * ============================
 */
export interface BookingFilters {
  userId?: number;
  propertyId?: number;
  status?: ReservationStatus;
  type?: 'upcoming' | 'past' | 'all';
}

/**
 * ============================
 * Interface BookingCalendar
 * Pour afficher les dates indisponibles
 * ============================
 */
export interface BookingCalendarDate {
  date: string;                      // Format: "2025-01-15"
  available: boolean;
  reservationId?: number;
  reason?: 'booked' | 'blocked' | 'past';
}

/**
 * ============================
 * Helpers / Utility Functions
 * ============================
 */

/**
 * Calculer le nombre de nuits entre deux dates
 */
export function calculateNights(checkIn: string, checkOut: string): number {
  const start = new Date(checkIn);
  const end = new Date(checkOut);
  const diffTime = Math.abs(end.getTime() - start.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

/**
 * Vérifier si une réservation est modifiable
 */
export function isBookingModifiable(booking: Booking): boolean {
  return booking.status === ReservationStatus.PENDING ||
    booking.status === ReservationStatus.CONFIRMED;
}

/**
 * Vérifier si une réservation est annulable
 */
export function isBookingCancellable(booking: Booking): boolean {
  return booking.status !== ReservationStatus.COMPLETED &&
    booking.status !== ReservationStatus.CANCELLED &&
    booking.status !== ReservationStatus.REFUNDED;
}

/**
 * Obtenir le label du statut en français
 */
export function getStatusLabel(status: ReservationStatus): string {
  const labels: Record<ReservationStatus, string> = {
    [ReservationStatus.PENDING]: 'En attente',
    [ReservationStatus.CONFIRMED]: 'Confirmée',
    [ReservationStatus.CHECKED_IN]: 'En cours',
    [ReservationStatus.COMPLETED]: 'Terminée',
    [ReservationStatus.CANCELLED]: 'Annulée',
    [ReservationStatus.REFUNDED]: 'Remboursée'
  };
  return labels[status];
}

/**
 * Obtenir la couleur du statut
 */
export function getStatusColor(status: ReservationStatus): string {
  const colors: Record<ReservationStatus, string> = {
    [ReservationStatus.PENDING]: '#FFA500',      // Orange
    [ReservationStatus.CONFIRMED]: '#00A699',    // Vert
    [ReservationStatus.CHECKED_IN]: '#0066FF',   // Bleu
    [ReservationStatus.COMPLETED]: '#6A6A6A',    // Gris
    [ReservationStatus.CANCELLED]: '#FF385C',    // Rouge
    [ReservationStatus.REFUNDED]: '#717171'      // Gris foncé
  };
  return colors[status];
}
