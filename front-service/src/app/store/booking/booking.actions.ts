// src/app/store/booking/booking.actions.ts

import { createAction, props } from '@ngrx/store';
import { Booking, CreateBookingDTO } from '../../core/models/booking.model';
import { BookingWithSnapshot } from '../../core/models/booking-with-snapshot.model';

/**
 * CRÉER UNE RÉSERVATION
 */
export const createBooking = createAction(
  '[Booking] Create Booking',
  props<{ booking: CreateBookingDTO }>()
);

export const createBookingSuccess = createAction(
  '[Booking] Create Booking Success',
  props<{ booking: Booking }>()
);

export const createBookingFailure = createAction(
  '[Booking] Create Booking Failure',
  props<{ error: string }>()
);

/**
 * CHARGER MES RÉSERVATIONS (simple)
 */
export const loadMyBookings = createAction('[Booking] Load My Bookings');

export const loadMyBookingsSuccess = createAction(
  '[Booking] Load My Bookings Success',
  props<{ bookings: Booking[] }>()
);

export const loadMyBookingsFailure = createAction(
  '[Booking] Load My Bookings Failure',
  props<{ error: string }>()
);

/**
 * CHARGER MES RÉSERVATIONS AVEC SNAPSHOTS
 */
export const loadMyBookingsWithSnapshots = createAction(
  '[Booking] Load My Bookings With Snapshots'
);

export const loadMyBookingsWithSnapshotsSuccess = createAction(
  '[Booking] Load My Bookings With Snapshots Success',
  props<{ bookings: BookingWithSnapshot[] }>()
);

export const loadMyBookingsWithSnapshotsFailure = createAction(
  '[Booking] Load My Bookings With Snapshots Failure',
  props<{ error: string }>()
);

/**
 * CHARGER UNE RÉSERVATION PAR ID (simple)
 */
export const loadBookingById = createAction(
  '[Booking] Load Booking By Id',
  props<{ id: number }>()
);

export const loadBookingByIdSuccess = createAction(
  '[Booking] Load Booking By Id Success',
  props<{ booking: Booking }>()
);

export const loadBookingByIdFailure = createAction(
  '[Booking] Load Booking By Id Failure',
  props<{ error: string }>()
);

/**
 * CHARGER BOOKING AVEC SNAPSHOT
 */
export const loadBookingWithSnapshot = createAction(
  '[Booking] Load Booking With Snapshot',
  props<{ id: number }>()
);

export const loadBookingWithSnapshotSuccess = createAction(
  '[Booking] Load Booking With Snapshot Success',
  props<{ booking: BookingWithSnapshot }>()
);

export const loadBookingWithSnapshotFailure = createAction(
  '[Booking] Load Booking With Snapshot Failure',
  props<{ error: string }>()
);

/**
 * CONFIRMER UNE RÉSERVATION
 */
export const confirmBooking = createAction(
  '[Booking] Confirm Booking',
  props<{ id: number; blockchainTxHash: string }>()
);

export const confirmBookingSuccess = createAction(
  '[Booking] Confirm Booking Success',
  props<{ booking: Booking }>()
);

export const confirmBookingFailure = createAction(
  '[Booking] Confirm Booking Failure',
  props<{ error: string }>()
);

/**
 * ANNULER UNE RÉSERVATION
 */
export const cancelBooking = createAction(
  '[Booking] Cancel Booking',
  props<{ id: number; reason?: string }>()
);

export const cancelBookingSuccess = createAction(
  '[Booking] Cancel Booking Success',
  props<{ booking: Booking }>()
);

export const cancelBookingFailure = createAction(
  '[Booking] Cancel Booking Failure',
  props<{ error: string }>()
);

/**
 * CHECK-IN / CHECK-OUT
 */
export const checkIn = createAction(
  '[Booking] Check In',
  props<{ id: number }>()
);

export const checkInSuccess = createAction(
  '[Booking] Check In Success',
  props<{ booking: Booking }>()
);

export const checkInFailure = createAction(
  '[Booking] Check In Failure',
  props<{ error: string }>()
);

export const checkOut = createAction(
  '[Booking] Check Out',
  props<{ id: number }>()
);

export const checkOutSuccess = createAction(
  '[Booking] Check Out Success',
  props<{ booking: Booking }>()
);

export const checkOutFailure = createAction(
  '[Booking] Check Out Failure',
  props<{ error: string }>()
);

/**
 * LIBÉRER L'ESCROW
 */
export const releaseEscrow = createAction(
  '[Booking] Release Escrow',
  props<{ id: number; txHash: string }>()
);

export const releaseEscrowSuccess = createAction(
  '[Booking] Release Escrow Success',
  props<{ booking: Booking }>()
);

export const releaseEscrowFailure = createAction(
  '[Booking] Release Escrow Failure',
  props<{ error: string }>()
);

/**
 * RESET / CLEAR
 */
export const resetBookingState = createAction('[Booking] Reset State');

export const clearBookingError = createAction('[Booking] Clear Error');
