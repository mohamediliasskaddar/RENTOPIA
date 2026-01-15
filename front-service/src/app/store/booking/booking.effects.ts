// src/app/store/booking/booking.effects.ts
// ✅ VERSION FINALE - COMPOSITION FRONTEND SEULEMENT

import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {of, forkJoin, Observable} from 'rxjs';
import { map, catchError, exhaustMap, tap, switchMap } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BookingService } from '../../core/services/booking.service';
import { PropertyVersionService } from '../../core/services/property-version.service';
import { UserService } from '../../core/services/user.service';
import * as BookingActions from './booking.actions';
import { Booking } from '../../core/models/booking.model';
import { BookingWithSnapshot, PropertySnapshotData } from '../../core/models/booking-with-snapshot.model';

@Injectable()
export class BookingEffects {

  private actions$ = inject(Actions);
  private bookingService = inject(BookingService);
  private propertyVersionService = inject(PropertyVersionService);
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);

  // ========================================
  // EFFECTS EXISTANTS (inchangés)
  // ========================================

  createBooking$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.createBooking),
      tap(action => console.log('🔥 Effect: createBooking', action)),
      exhaustMap(({ booking }) =>
        this.bookingService.createBooking(booking).pipe(
          tap(() => this.snackBar.open('Booking created successfully', 'Close', { duration: 3000 })),
          map(createdBooking => BookingActions.createBookingSuccess({ booking: createdBooking })),
          catchError(error => {
            this.snackBar.open('Error creating booking', 'Close', { duration: 3000 });
            return of(BookingActions.createBookingFailure({
              error: error.message || 'Failed to create booking'
            }));
          })
        )
      )
    )
  );

  loadMyBookings$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.loadMyBookings),
      tap(() => console.log('🔥 Effect: loadMyBookings')),
      exhaustMap(() =>
        this.bookingService.getMyBookings().pipe(
          map(bookings => BookingActions.loadMyBookingsSuccess({ bookings })),
          catchError(error => of(BookingActions.loadMyBookingsFailure({
            error: error.message || 'Failed to load bookings'
          })))
        )
      )
    )
  );

  loadBookingById$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.loadBookingById),
      exhaustMap(({ id }) =>
        this.bookingService.getBookingById(id).pipe(
          map(booking => BookingActions.loadBookingByIdSuccess({ booking })),
          catchError(error => of(BookingActions.loadBookingByIdFailure({
            error: error.message || 'Failed to load booking'
          })))
        )
      )
    )
  );

  confirmBooking$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.confirmBooking),
      exhaustMap(({ id, blockchainTxHash }) =>
        this.bookingService.confirmBooking(id, blockchainTxHash).pipe(
          tap(() => this.snackBar.open('Booking confirmed', 'Close', { duration: 3000 })),
          map(booking => BookingActions.confirmBookingSuccess({ booking })),
          catchError(error => {
            this.snackBar.open('Error confirming booking', 'Close', { duration: 3000 });
            return of(BookingActions.confirmBookingFailure({
              error: error.message || 'Failed to confirm booking'
            }));
          })
        )
      )
    )
  );

  cancelBooking$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.cancelBooking),
      exhaustMap(({ id, reason }) =>
        this.bookingService.cancelBooking(id, reason || 'Cancelled by user').pipe(
          tap(() => this.snackBar.open('Booking cancelled', 'Close', { duration: 3000 })),
          map(booking => BookingActions.cancelBookingSuccess({ booking })),
          catchError(error => {
            this.snackBar.open('Error cancelling booking', 'Close', { duration: 3000 });
            return of(BookingActions.cancelBookingFailure({
              error: error.message || 'Failed to cancel booking'
            }));
          })
        )
      )
    )
  );

  checkIn$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.checkIn),
      exhaustMap(({ id }) =>
        this.bookingService.checkIn(id).pipe(
          tap(() => this.snackBar.open('Check-in successful', 'Close', { duration: 3000 })),
          map(booking => BookingActions.checkInSuccess({ booking })),
          catchError(error => {
            this.snackBar.open('Error during check-in', 'Close', { duration: 3000 });
            return of(BookingActions.checkInFailure({
              error: error.message || 'Failed to check-in'
            }));
          })
        )
      )
    )
  );

  checkOut$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.checkOut),
      exhaustMap(({ id }) =>
        this.bookingService.checkOut(id).pipe(
          tap(() => this.snackBar.open('Check-out successful', 'Close', { duration: 3000 })),
          map(booking => BookingActions.checkOutSuccess({ booking })),
          catchError(error => {
            this.snackBar.open('Error during check-out', 'Close', { duration: 3000 });
            return of(BookingActions.checkOutFailure({
              error: error.message || 'Failed to check-out'
            }));
          })
        )
      )
    )
  );

  releaseEscrow$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.releaseEscrow),
      exhaustMap(({ id, txHash }) =>
        this.bookingService.releaseEscrow(id, txHash).pipe(
          tap(() => this.snackBar.open('Escrow released', 'Close', { duration: 3000 })),
          map(booking => BookingActions.releaseEscrowSuccess({ booking })),
          catchError(error => {
            this.snackBar.open('Error releasing escrow', 'Close', { duration: 3000 });
            return of(BookingActions.releaseEscrowFailure({
              error: error.message || 'Failed to release escrow'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // ✅ NOUVEAUX EFFECTS - COMPOSITION FRONTEND
  // ========================================

  /**
   * CHARGER MES RÉSERVATIONS AVEC SNAPSHOTS
   * Compose bookings + property versions côté frontend
   */
  loadMyBookingsWithSnapshots$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.loadMyBookingsWithSnapshots),
      tap(() => console.log('🔥 Effect: loadMyBookingsWithSnapshots')),
      exhaustMap(() =>
        // 1. Charger les bookings simples
        this.bookingService.getMyBookings().pipe(
          switchMap((bookings: Booking[]) => {
            if (bookings.length === 0) {
              return of(BookingActions.loadMyBookingsWithSnapshotsSuccess({ bookings: [] }));
            }

            // 2. Pour chaque booking, enrichir avec PropertyVersion
            const enrichedBookings$ = bookings.map(booking =>
              this.enrichBookingWithSnapshot(booking)
            );

            // 3. Attendre que tous soient enrichis
            return forkJoin(enrichedBookings$).pipe(
              tap(enriched => console.log('✅ Bookings enriched:', enriched.length)),
              map(enrichedBookings =>
                BookingActions.loadMyBookingsWithSnapshotsSuccess({ bookings: enrichedBookings })
              )
            );
          }),
          catchError(error => {
            console.error('❌ Error loading bookings with snapshots:', error);
            this.snackBar.open('Error loading bookings', 'Close', { duration: 3000 });
            return of(BookingActions.loadMyBookingsWithSnapshotsFailure({
              error: error.message || 'Failed to load bookings'
            }));
          })
        )
      )
    )
  );

  /**
   * CHARGER UN BOOKING AVEC SNAPSHOT PAR ID
   */
  loadBookingWithSnapshot$ = createEffect(() =>
    this.actions$.pipe(
      ofType(BookingActions.loadBookingWithSnapshot),
      tap(action => console.log('🔥 Effect: loadBookingWithSnapshot', action)),
      exhaustMap(({ id }) =>
        // 1. Charger le booking
        this.bookingService.getBookingById(id).pipe(
          switchMap(booking =>
            // 2. Enrichir avec snapshot
            this.enrichBookingWithSnapshot(booking)
          ),
          tap(enriched => console.log('✅ Booking enriched:', enriched)),
          map(enrichedBooking =>
            BookingActions.loadBookingWithSnapshotSuccess({ booking: enrichedBooking })
          ),
          catchError(error => {
            console.error('❌ Error loading booking with snapshot:', error);
            this.snackBar.open('Error loading booking details', 'Close', { duration: 3000 });
            return of(BookingActions.loadBookingWithSnapshotFailure({
              error: error.message || 'Failed to load booking details'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // ✅ MÉTHODE PRIVÉE - ENRICHISSEMENT
  // ========================================

  /**
   * Enrichir un booking avec les données de PropertyVersion
   */
  private enrichBookingWithSnapshot(booking: Booking): Observable<BookingWithSnapshot> {
    // Si pas de versionId, retourner booking sans enrichissement
    if (!booking.versionId) {
      console.warn('⚠️ Booking has no versionId:', booking.id);
      return of(this.createMinimalBookingWithSnapshot(booking));
    }

    return this.propertyVersionService.getVersionById(booking.versionId).pipe(
      switchMap(version => {
        // Parser les snapshots JSON
        const propertySnapshot = this.parsePropertySnapshot(version);

        // Récupérer les infos de l'hôte (optionnel)
        // Note: hostId n'est pas dans votre modèle Booking actuel
        // Si vous l'ajoutez plus tard, décommentez cette partie
        /*
        return this.userService.getUserById(hostId).pipe(
          map(host => ({
            bookingId: booking.id,
            propertyId: booking.propertyId,
            userId: booking.userId,
            hostId: host.id,
            checkIn: booking.checkInDate,
            checkOut: booking.checkOutDate,
            totalPrice: booking.priceBreakdown.totalAmount,
            status: booking.status,
            createdAt: booking.createdAt,
            propertySnapshot,
            hostName: `${host.prenom} ${host.nom}`,
            hostPhoto: host.photoUrl
          }))
        );
        */

        // Version sans infos hôte
        return of({
          bookingId: booking.id,
          propertyId: booking.propertyId,
          userId: booking.userId,
          hostId: 0,  // À remplacer si vous ajoutez hostId au booking
          checkIn: booking.checkInDate,
          checkOut: booking.checkOutDate,
          totalPrice: booking.priceBreakdown.totalAmount,
          status: booking.status,
          createdAt: booking.createdAt,
          propertySnapshot
        } as BookingWithSnapshot);
      }),
      catchError(error => {
        console.warn('⚠️ Could not load version for booking:', booking.id, error);
        return of(this.createMinimalBookingWithSnapshot(booking));
      })
    );
  }

  /**
   * Parser les snapshots JSON de PropertyVersion
   */
  private parsePropertySnapshot(version: any): PropertySnapshotData {
    try {
      // Parser generalJson
      const general = JSON.parse(version.generalSnapshot.generalJson);

      // Parser photosJson
      const photos = JSON.parse(version.photosSnapshot.photosJson);

      // Parser amenitiesJson
      const amenities = JSON.parse(version.amenitiesSnapshot.amenitiesJson);

      // Rules déjà en objet
      const rules = version.rulesSnapshot;

      return {
        // General
        title: general.title,
        description: general.description,
        propertyType: general.propertyType,
        placeType: general.placeType,
        city: general.city,
        country: general.country,
        address: general.adresseLine,

        // Characteristics
        maxGuests: general.maxGuests,
        bedrooms: general.bedrooms,
        beds: general.beds,
        bathrooms: general.bathrooms,
        surfaceArea: general.surfaceArea,
        floorNumber: general.floorNumber,

        // Prices
        pricePerNight: general.pricePerNight,
        weekendPricePerNight: general.weekendPricePerNight,
        cleaningFee: general.cleaningFee,
        petFee: general.petFee,

        // Check-in/out
        checkInTimeStart: general.checkInTimeStart,
        checkInTimeEnd: general.checkInTimeEnd,
        checkOutTime: general.checkOutTime,

        // Photos
        photos: photos.map((p: any) => ({
          photoUrl: p.photoUrl,
          isCover: p.isCover,
          displayOrder: p.displayOrder
        })),

        // Amenities
        amenities: amenities.map((a: any) => ({
          amenityId: a.amenityId,
          name: a.name,
          category: a.category,
          icon: a.icone
        })),

        // Rules
        rules: {
          childrenAllowed: rules.childrenAllowed,
          babiesAllowed: rules.babiesAllowed,
          petsAllowed: rules.petsAllowed,
          smokingAllowed: rules.smokingAllowed,
          eventsAllowed: rules.eventsAllowed,
          customRules: rules.customRules
        }
      };
    } catch (error) {
      console.error('❌ Error parsing property snapshot:', error);
      throw error;
    }
  }

  /**
   * Créer un BookingWithSnapshot minimal si pas de version
   */
  private createMinimalBookingWithSnapshot(booking: Booking): BookingWithSnapshot {
    return {
      bookingId: booking.id,
      propertyId: booking.propertyId,
      userId: booking.userId,
      hostId: 0,
      checkIn: booking.checkInDate,
      checkOut: booking.checkOutDate,
      totalPrice: booking.priceBreakdown.totalAmount,
      status: booking.status,
      createdAt: booking.createdAt,
      propertySnapshot: {
        title: 'Property information unavailable',
        description: '',
        propertyType: '',
        placeType: '',
        city: '',
        country: '',
        address: '',
        maxGuests: 0,
        bedrooms: 0,
        beds: 0,
        bathrooms: 0,
        pricePerNight: booking.priceBreakdown.lockedPricePerNight,
        photos: [],
        amenities: [],
        rules: {
          childrenAllowed: false,
          babiesAllowed: false,
          petsAllowed: false,
          smokingAllowed: false,
          eventsAllowed: false,
          customRules: ''
        }
      }
    };
  }
}
