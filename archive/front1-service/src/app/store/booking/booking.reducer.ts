// src/app/store/booking/booking.reducer.ts

import { createReducer, on } from '@ngrx/store';
import { Booking } from '../../core/models/booking.model';
import { BookingWithSnapshot } from '../../core/models/booking-with-snapshot.model';
import * as BookingActions from './booking.actions';

export interface BookingState {
  bookings: Booking[];
  bookingsWithSnapshots: BookingWithSnapshot[];
  currentBooking: Booking | null;
  currentBookingWithSnapshot: BookingWithSnapshot | null;
  loading: boolean;
  error: string | null;
}

export const initialState: BookingState = {
  bookings: [],
  bookingsWithSnapshots: [],
  currentBooking: null,
  currentBookingWithSnapshot: null,
  loading: false,
  error: null
};

export const bookingReducer = createReducer(
  initialState,

  // CREATE BOOKING
  on(BookingActions.createBooking, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.createBookingSuccess, (state, { booking }) => ({
    ...state,
    bookings: [...state.bookings, booking],
    currentBooking: booking,
    loading: false
  })),

  on(BookingActions.createBookingFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // LOAD MY BOOKINGS (simple)
  on(BookingActions.loadMyBookings, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.loadMyBookingsSuccess, (state, { bookings }) => ({
    ...state,
    bookings,
    loading: false
  })),

  on(BookingActions.loadMyBookingsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // LOAD MY BOOKINGS WITH SNAPSHOTS
  on(BookingActions.loadMyBookingsWithSnapshots, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.loadMyBookingsWithSnapshotsSuccess, (state, { bookings }) => ({
    ...state,
    bookingsWithSnapshots: bookings,
    loading: false
  })),

  on(BookingActions.loadMyBookingsWithSnapshotsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // LOAD BOOKING BY ID (simple)
  on(BookingActions.loadBookingById, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.loadBookingByIdSuccess, (state, { booking }) => ({
    ...state,
    currentBooking: booking,
    loading: false
  })),

  on(BookingActions.loadBookingByIdFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // LOAD BOOKING WITH SNAPSHOT
  on(BookingActions.loadBookingWithSnapshot, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.loadBookingWithSnapshotSuccess, (state, { booking }) => ({
    ...state,
    currentBookingWithSnapshot: booking,
    loading: false
  })),

  on(BookingActions.loadBookingWithSnapshotFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // CONFIRM BOOKING
  on(BookingActions.confirmBooking, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.confirmBookingSuccess, (state, { booking }) => ({
    ...state,
    bookings: state.bookings.map(b => b.id === booking.id ? booking : b),
    currentBooking: booking,
    loading: false
  })),

  on(BookingActions.confirmBookingFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // CANCEL BOOKING
  on(BookingActions.cancelBooking, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.cancelBookingSuccess, (state, { booking }) => ({
    ...state,
    bookings: state.bookings.map(b => b.id === booking.id ? booking : b),
    bookingsWithSnapshots: state.bookingsWithSnapshots.map(b =>
      b.bookingId === booking.id ? { ...b, status: booking.status } : b
    ),
    currentBooking: booking,
    loading: false
  })),

  on(BookingActions.cancelBookingFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // CHECK-IN
  on(BookingActions.checkIn, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.checkInSuccess, (state, { booking }) => ({
    ...state,
    bookings: state.bookings.map(b => b.id === booking.id ? booking : b),
    currentBooking: booking,
    loading: false
  })),

  on(BookingActions.checkInFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // CHECK-OUT
  on(BookingActions.checkOut, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.checkOutSuccess, (state, { booking }) => ({
    ...state,
    bookings: state.bookings.map(b => b.id === booking.id ? booking : b),
    currentBooking: booking,
    loading: false
  })),

  on(BookingActions.checkOutFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // RELEASE ESCROW
  on(BookingActions.releaseEscrow, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(BookingActions.releaseEscrowSuccess, (state, { booking }) => ({
    ...state,
    bookings: state.bookings.map(b => b.id === booking.id ? booking : b),
    currentBooking: booking,
    loading: false
  })),

  on(BookingActions.releaseEscrowFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // RESET / CLEAR
  on(BookingActions.resetBookingState, () => initialState),

  on(BookingActions.clearBookingError, (state) => ({
    ...state,
    error: null
  }))
);
