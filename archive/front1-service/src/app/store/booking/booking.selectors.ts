// src/app/store/booking/booking.selectors.ts

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { BookingState } from './booking.reducer';
import { Booking, ReservationStatus } from '../../core/models/booking.model';
import { BookingWithSnapshot } from '../../core/models/booking-with-snapshot.model';
import {selectCurrentUser} from "../auth/auth.selectors";

export const selectBookingState = createFeatureSelector<BookingState>('booking');

// ========================================
// SÉLECTEURS DE BASE
// ========================================

export const selectMyBookings = createSelector(
  selectBookingState,
  (state: BookingState) => state.bookings
);

export const selectMyBookingsWithSnapshots = createSelector(
  selectBookingState,
  (state: BookingState) => state.bookingsWithSnapshots
);

export const selectCurrentBooking = createSelector(
  selectBookingState,
  (state: BookingState) => state.currentBooking
);

export const selectCurrentBookingWithSnapshot = createSelector(
  selectBookingState,
  (state: BookingState) => state.currentBookingWithSnapshot
);

export const selectBookingLoading = createSelector(
  selectBookingState,
  (state: BookingState) => state.loading
);

export const selectBookingError = createSelector(
  selectBookingState,
  (state: BookingState) => state.error
);

// ========================================
// SÉLECTEURS AVEC SNAPSHOTS
// ========================================

export const selectUpcomingBookingsWithSnapshots = createSelector(
  selectMyBookingsWithSnapshots,
  (bookings: BookingWithSnapshot[]) => {
    const now = new Date();
    return bookings.filter(b =>
      new Date(b.checkIn) > now &&
      (b.status === ReservationStatus.CONFIRMED || b.status === ReservationStatus.PENDING)
    ).sort((a, b) => new Date(a.checkIn).getTime() - new Date(b.checkIn).getTime());
  }
);

export const selectPastBookingsWithSnapshots = createSelector(
  selectMyBookingsWithSnapshots,
  (bookings: BookingWithSnapshot[]) => {
    const now = new Date();
    return bookings.filter(b =>
      new Date(b.checkOut) < now &&
      b.status === ReservationStatus.COMPLETED
    ).sort((a, b) => new Date(b.checkOut).getTime() - new Date(a.checkOut).getTime());
  }
);

export const selectCancelledBookingsWithSnapshots = createSelector(
  selectMyBookingsWithSnapshots,
  (bookings: BookingWithSnapshot[]) =>
    bookings.filter(b => b.status === ReservationStatus.CANCELLED)
);

export const selectBookingStatsWithSnapshots = createSelector(
  selectMyBookingsWithSnapshots,
  (bookings: BookingWithSnapshot[]) => {
    const now = new Date();

    const upcoming = bookings.filter(b =>
      new Date(b.checkIn) > now &&
      (b.status === ReservationStatus.CONFIRMED || b.status === ReservationStatus.PENDING)
    ).length;

    const past = bookings.filter(b =>
      new Date(b.checkOut) < now &&
      b.status === ReservationStatus.COMPLETED
    ).length;

    const cancelled = bookings.filter(b => b.status === ReservationStatus.CANCELLED).length;

    const totalSpent = bookings
      .filter(b =>
        b.status === ReservationStatus.CONFIRMED ||
        b.status === ReservationStatus.COMPLETED
      )
      .reduce((sum, b) => sum + b.totalPrice, 0);

    return {
      total: bookings.length,
      upcoming,
      past,
      cancelled,
      totalSpent
    };
  }
);

export const selectNextBookingWithSnapshot = createSelector(
  selectUpcomingBookingsWithSnapshots,
  (bookings: BookingWithSnapshot[]) => bookings[0] || null
);

export const selectBookingWithSnapshotById = (bookingId: number) =>
  createSelector(
    selectMyBookingsWithSnapshots,
    (bookings: BookingWithSnapshot[]) =>
      bookings.find(b => b.bookingId === bookingId) || null
  );

// ========================================
// SÉLECTEURS SIMPLES (legacy)
// ========================================

export const selectUpcomingBookings = createSelector(
  selectMyBookings,
  (bookings: Booking[]) => {
    const now = new Date();
    return bookings.filter(b =>
      new Date(b.checkInDate) > now &&
      (b.status === ReservationStatus.CONFIRMED || b.status === ReservationStatus.PENDING)
    );
  }
);

export const selectPastBookings = createSelector(
  selectMyBookings,
  (bookings: Booking[]) => {
    const now = new Date();
    return bookings.filter(b =>
      new Date(b.checkOutDate) < now &&
      b.status === ReservationStatus.COMPLETED
    );
  }
);

export const selectTotalBookingsCount = createSelector(
  selectMyBookings,
  (bookings: Booking[]) => bookings.length
);

export const selectBookingById = (bookingId: number) =>
  createSelector(
    selectMyBookings,
    (bookings: Booking[]) => bookings.find(b => b.id === bookingId)
  );


// src/app/store/booking/booking.selectors.ts (AJOUTER)
export const selectBookingsWithReviewInfo = createSelector(
  selectMyBookingsWithSnapshots,
  selectCurrentUser,
  (bookings, currentUser) => {
    if (!bookings || !currentUser) return bookings;

    return bookings.map(booking => {
      // Pour l'instant, retourner les bookings sans enrichissement
      // L'enrichissement se fera dans le composant
      return {
        ...booking,
        userCanReview: false, // À calculer dans le composant
        hasReview: false      // À calculer dans le composant
      };
    });
  }
);
