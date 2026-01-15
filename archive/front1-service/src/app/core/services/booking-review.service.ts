// src/app/core/services/booking-review.service.ts
// NOUVELLE VERSION SIMPLIFIÉE

import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReviewService } from './review.service';
import { BookingWithSnapshot } from '../models/booking-with-snapshot.model';
import { ReservationStatus } from '../models/booking.model';
import { Review } from '../models/review.model';

@Injectable({
  providedIn: 'root'
})
export class BookingReviewService {

  private reviewService = inject(ReviewService);

  /**
   * NOUVELLE MÉTHODE : Récupère un review par reservationId
   * Utilise l'endpoint direct /reviews/reservation/{reservationId}
   */
  getReviewByReservationId(reservationId: number): Observable<Review | null> {
    console.log(`🔍 Appel direct getReviewByReservationId(${reservationId})`);

    // Note: Vous devrez peut-être ajouter cette méthode à ReviewService
    return this.reviewService.getReviewByReservationId(reservationId).pipe(
      map(review => {
        console.log(`✅ Review trouvé pour reservation ${reservationId}:`, review);
        return review;
      }),
      catchError(error => {
        // Si 404 = pas de review, retourne null
        if (error.status === 404) {
          console.log(`ℹ️ Pas de review pour reservation ${reservationId}`);
          return of(null);
        }
        console.error(`❌ Erreur getReviewByReservationId(${reservationId}):`, error);
        return of(null);
      })
    );
  }

  /**
   * VERSION SIMPLIFIÉE de enrichBookingsWithReviewInfo
   * Utilise l'endpoint direct par reservationId
   */
  enrichBookingsWithReviewInfo(
    bookings: BookingWithSnapshot[],
    userId: number
  ): Observable<BookingWithSnapshot[]> {

    console.log('🔍 enrichBookingsWithReviewInfo - VERSION SIMPLIFIÉE');
    console.log(`  Bookings: ${bookings.length}, UserId: ${userId}`);

    if (bookings.length === 0) {
      return of([]);
    }

    // 1. Pour chaque booking, appelle l'endpoint direct
    const reviewObservables = bookings.map(booking =>
      this.getReviewByReservationId(booking.bookingId).pipe(
        map(review => ({
          booking,
          review
        }))
      )
    );

    // 2. Combine tous les résultats
    return forkJoin(reviewObservables).pipe(
      map(results => {
        console.log('📊 Résultats combinés:');

        return results.map(({ booking, review }) => {
          const hasReview = !!review;
          const userCanReview = this.canUserReviewBooking(booking, hasReview);

          console.log(`  Booking ${booking.bookingId}:`, {
            hasReview,
            reviewId: review?.id,
            userCanReview,
            status: booking.status,
            checkOut: booking.checkOut,

          });

          return {
            ...booking,
            hasReview,
            reviewId: review?.id,
            userCanReview,
            review: review ?? undefined

          };
        });
      }),
      catchError(error => {
        console.error('❌ Erreur dans enrichBookingsWithReviewInfo:', error);
        return of(bookings.map(booking => ({
          ...booking,
          hasReview: false,
          userCanReview: this.canUserReviewBooking(booking, false)
        })));
      })
    );
  }

  /**
   * Logique inchangée
   */
  private canUserReviewBooking(booking: BookingWithSnapshot, hasReview: boolean): boolean {
    // 1. Vérifier le statut
    const isCompleted = booking.status === ReservationStatus.COMPLETED;
    if (!isCompleted) {
      return false;
    }

    // 2. Vérifier que le check-out est passé
    const checkOutDate = new Date(booking.checkOut);
    const now = new Date();
    if (checkOutDate > now) {
      return false;
    }

    // 3. Vérifier que l'utilisateur n'a pas déjà révisé
    if (hasReview) {
      return false;
    }

    return true;
  }
}
