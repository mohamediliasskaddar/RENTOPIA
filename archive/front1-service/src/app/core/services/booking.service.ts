// src/app/core/services/booking.service.ts

import { inject, Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {
  Booking,
  CreateBookingDTO,
  ReservationStatus
} from '../models/booking.model';
import { BookingWithSnapshot } from '../models/booking-with-snapshot.model';
import { catchError, tap } from "rxjs/operators";
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BookingService {

  private http = inject(HttpClient);
  // ✅ Utilisation de l'URL depuis environment
  private baseUrl = environment.apiUrl.replace('/api', ''); // Enlève '/api' pour avoir 'http://localhost:8080'

  // Alternative si vous voulez garder '/api' :
  // private baseUrl = environment.apiUrl; // 'http://localhost:8080/api'

  constructor() {
    console.log('✅ BookingService initialized');
    console.log('📍 Base URL:', this.baseUrl);
  }

  /**
   * Headers avec token
   */
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem(environment.tokenKey);
    console.log('🔑 Token from localStorage:', token ? token.substring(0, 50) + '...' : 'NULL');

    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
      console.log('🔑 Authorization header set');
    } else {
      console.error('❌ No token found in localStorage!');
    }

    return headers;
  }

  /**
   * CRÉER UNE RÉSERVATION
   * POST http://localhost:8080/bookings/new
   */
  createBooking(booking: CreateBookingDTO): Observable<Booking> {
    console.log('📤 Creating booking:', booking);
    console.log('📤 JSON being sent:', JSON.stringify(booking));

    return this.http.post<Booking>(
      `${this.baseUrl}/bookings/new`,
      booking,
      { headers: this.getHeaders() }
    ).pipe(
      tap(response => console.log('✅ Booking created:', response)),
      catchError(error => {
        console.error('❌ Backend error status:', error.status);
        console.error('❌ Backend error message:', error.error);
        console.error('❌ Full error:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * MES RÉSERVATIONS
   * GET http://localhost:8080/bookings/user/me
   */
  getMyBookings(): Observable<Booking[]> {
    console.log('📤 GET', `${this.baseUrl}/bookings/user/me`);
    return this.http.get<Booking[]>(
      `${this.baseUrl}/bookings/user/me`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * RÉSERVATIONS À VENIR
   * GET http://localhost:8080/bookings/user/upcoming
   */
  getUpcomingBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(
      `${this.baseUrl}/bookings/user/upcoming`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * RÉSERVATIONS PASSÉES
   * GET http://localhost:8080/bookings/user/past
   */
  getPastBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(
      `${this.baseUrl}/bookings/user/past`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * RÉCUPÉRER UNE RÉSERVATION PAR ID
   * GET http://localhost:8080/bookings/{id}
   */
  getBookingById(id: number): Observable<Booking> {
    return this.http.get<Booking>(
      `${this.baseUrl}/bookings/${id}`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * RÉCUPÉRER LES RÉSERVATIONS D'UNE PROPRIÉTÉ
   * GET http://localhost:8080/bookings/property/{propertyId}
   */
  getPropertyBookings(propertyId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(
      `${this.baseUrl}/bookings/property/${propertyId}`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * CONFIRMER UNE RÉSERVATION
   * PATCH http://localhost:8080/bookings/{id}/confirm?blockchainTxHash=xxx
   */
  confirmBooking(id: number, blockchainTxHash: string): Observable<Booking> {
    return this.http.patch<Booking>(
      `${this.baseUrl}/bookings/${id}/confirm?blockchainTxHash=${blockchainTxHash}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * CHECK-IN
   * PATCH http://localhost:8080/bookings/{id}/check-in
   */
  checkIn(id: number): Observable<Booking> {
    return this.http.patch<Booking>(
      `${this.baseUrl}/bookings/${id}/check-in`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * CHECK-OUT
   * PATCH http://localhost:8080/bookings/{id}/check-out
   */
  checkOut(id: number): Observable<Booking> {
    return this.http.patch<Booking>(
      `${this.baseUrl}/bookings/${id}/check-out`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * ANNULER UNE RÉSERVATION
   * PATCH http://localhost:8080/bookings/{id}/cancel?reason=xxx
   */
  cancelBooking(id: number, reason: string): Observable<Booking> {
    return this.http.patch<Booking>(
      `${this.baseUrl}/bookings/${id}/cancel?reason=${encodeURIComponent(reason)}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * LIBÉRER L'ESCROW
   * PATCH http://localhost:8080/bookings/{id}/release-escrow?txHash=xxx
   */
  releaseEscrow(id: number, txHash: string): Observable<Booking> {
    return this.http.patch<Booking>(
      `${this.baseUrl}/bookings/${id}/release-escrow?txHash=${txHash}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * RÉSERVATIONS EN TANT QUE HOST
   * GET http://localhost:8080/bookings/host/me
   */
  getHostBookings(): Observable<Booking[]> {
    console.log('📤 GET', `${this.baseUrl}/bookings/host/me`);
    return this.http.get<Booking[]>(
      `${this.baseUrl}/bookings/host/me`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * RÉSERVATIONS DU HOST PAR STATUT
   * GET http://localhost:8080/bookings/host/me/status?status=CONFIRMED
   */
  getHostBookingsByStatus(status: ReservationStatus): Observable<Booking[]> {
    return this.http.get<Booking[]>(
      `${this.baseUrl}/bookings/host/me/status?status=${status}`,
      { headers: this.getHeaders() }
    );
  }
}
