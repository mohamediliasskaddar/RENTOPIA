// src/app/features/my-bookings/my-bookings.component.ts

import { Component, OnInit, OnDestroy, inject, AfterViewInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatDialog } from '@angular/material/dialog';
import { combineLatest, Observable, Subject } from 'rxjs';
import { filter, switchMap, takeUntil, take } from 'rxjs/operators';

// Material
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';

// Store
import * as BookingActions from '../../store/booking/booking.actions';
import {
  selectMyBookingsWithSnapshots,
  selectUpcomingBookingsWithSnapshots,
  selectPastBookingsWithSnapshots,
  selectCancelledBookingsWithSnapshots,
  selectBookingStatsWithSnapshots,
  selectBookingLoading,
  selectBookingWithSnapshotById
} from '../../store/booking/booking.selectors';
import { selectCurrentUser } from '../../store/auth/auth.selectors';

// Models
import { BookingWithSnapshot } from '../../core/models/booking-with-snapshot.model';

// Components
import { BookingCardComponent } from './booking-card/booking-card.component';
import { BookingDetailDialogComponent } from './booking-detail-dialog/booking-detail-dialog.component';

// Services
import { BookingReviewService } from '../../core/services/booking-review.service';
import {EthPricePipe} from "../../core/pipes/eth-price.pipe";

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    BookingCardComponent,
    EthPricePipe
  ],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.scss'
})
export class MyBookingsComponent implements OnInit, AfterViewInit, OnDestroy {

  private store = inject(Store);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private bookingReviewService = inject(BookingReviewService);
  private destroy$ = new Subject<void>();
  private bookingDialogToOpen: number | null = null;

  // Observables
  allBookings$ = this.store.select(selectMyBookingsWithSnapshots);
  upcomingBookings$ = this.store.select(selectUpcomingBookingsWithSnapshots);
  pastBookings$ = this.store.select(selectPastBookingsWithSnapshots);
  cancelledBookings$ = this.store.select(selectCancelledBookingsWithSnapshots);
  stats$ = this.store.select(selectBookingStatsWithSnapshots);
  loading$ = this.store.select(selectBookingLoading);
  user$ = this.store.select(selectCurrentUser);

  pastBookingsWithReviews$!: Observable<BookingWithSnapshot[]>;
  allBookingsWithReviews$!: Observable<BookingWithSnapshot[]>;

  selectedTab = 0;

  /**
   * ✅ NOUVEAU : Écouter l'événement personnalisé pour ouvrir le dialog
   */
  @HostListener('window:openBookingDialog', ['$event'])
  onOpenBookingDialogEvent(event: CustomEvent): void {
    console.log('🎯 Received openBookingDialog event:', event.detail);
    if (event.detail?.bookingId) {
      this.openBookingDialog(event.detail.bookingId);
    }
  }

  ngOnInit(): void {
    console.log('🎬 MyBookingsComponent - Init');

    // Charger les bookings
    this.store.dispatch(BookingActions.loadMyBookingsWithSnapshots());

    // Enrichir avec review info
    this.pastBookingsWithReviews$ = combineLatest([
      this.pastBookings$,
      this.user$
    ]).pipe(
      filter(([bookings, user]) => !!bookings && !!user),
      switchMap(([bookings, user]) => {
        return this.bookingReviewService.enrichBookingsWithReviewInfo(
          bookings,
          user?.id || 0
        );
      }),
      takeUntil(this.destroy$)
    );

    this.allBookingsWithReviews$ = combineLatest([
      this.allBookings$,
      this.user$
    ]).pipe(
      filter(([bookings, user]) => !!bookings && !!user),
      switchMap(([bookings, user]) => {
        return this.bookingReviewService.enrichBookingsWithReviewInfo(
          bookings,
          user?.id || 0
        );
      }),
      takeUntil(this.destroy$)
    );

    // ✅ Vérifier le state de navigation
    this.checkForBookingDialog();
  }

  ngAfterViewInit(): void {
    // ✅ Si un dialog doit être ouvert, le faire après que la vue soit initialisée
    if (this.bookingDialogToOpen) {
      console.log('🎯 Opening dialog after view init:', this.bookingDialogToOpen);
      setTimeout(() => {
        this.openBookingDialog(this.bookingDialogToOpen!);
        this.bookingDialogToOpen = null;
      }, 300);
    }
  }

  /**
   * ✅ Vérifier plusieurs sources pour le bookingId
   */
  private checkForBookingDialog(): void {
    console.log('🔍 Checking for booking dialog to open...');

    // Source 1: Navigation courante (fonctionne pendant la navigation)
    const currentNav = this.router.getCurrentNavigation();
    if (currentNav?.extras?.state?.['openBookingDialog']) {
      const bookingId = currentNav.extras.state['openBookingDialog'];
      console.log('✅ Found in getCurrentNavigation:', bookingId);
      this.bookingDialogToOpen = bookingId;
      return;
    }

    // Source 2: window.history.state (fonctionne après navigation/reload)
    const historyState = window.history.state;
    if (historyState?.openBookingDialog) {
      const bookingId = historyState.openBookingDialog;
      console.log('✅ Found in window.history.state:', bookingId);
      this.bookingDialogToOpen = bookingId;

      // Nettoyer le state pour éviter de rouvrir au refresh
      window.history.replaceState({}, '', window.location.pathname);
      return;
    }

    console.log('ℹ️ No booking dialog to open');
  }

  /**
   * ✅ Méthode dédiée pour ouvrir le dialog
   */
  private openBookingDialog(bookingId: number): void {
    console.log('🚀 Opening dialog for booking:', bookingId);

    // Vérifier si un dialog est déjà ouvert
    if (this.dialog.openDialogs.length > 0) {
      console.log('⚠️ Dialog already open, closing first');
      this.dialog.closeAll();
    }

    // Attendre que les bookings soient chargés
    this.allBookings$.pipe(
      filter(bookings => bookings && bookings.length > 0),
      take(1)
    ).subscribe(bookings => {
      console.log('📚 Bookings loaded, total:', bookings.length);

      // Trouver le booking
      const booking = bookings.find(b => b.bookingId === bookingId);

      if (!booking) {
        console.error('❌ Booking not found:', bookingId);
        console.log('Available bookings:', bookings.map(b => b.bookingId));
        return;
      }

      console.log('✅ Found booking:', booking);

      const dialogRef = this.dialog.open(BookingDetailDialogComponent, {
        width: '95vw',
        maxWidth: '850px',
        maxHeight: '95vh',
        panelClass: 'booking-detail-dialog-container', // ✅ Permet le centrage via CSS
        backdropClass: 'booking-detail-dialog-backdrop', // ✅ Fond personnalisé
        hasBackdrop: true,
        disableClose: false,
        autoFocus: false,
        data: {
          bookingId,
          booking
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('🔒 Dialog closed:', result);
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onViewDetails(bookingId: number): void {
    console.log('👁️ View details clicked:', bookingId);
    this.openBookingDialog(bookingId);
  }

  onCancelBooking(bookingId: number): void {
    console.log('❌ Cancel booking:', bookingId);
    this.store.dispatch(BookingActions.cancelBooking({
      id: bookingId,
      reason: 'Cancelled by user'
    }));
  }

  onViewProperty(propertyId: number): void {
    console.log('🏠 View property:', propertyId);
    this.router.navigate(['/property', propertyId]);
  }

  onTabChange(index: number): void {
    this.selectedTab = index;
  }

  onReviewSubmitted(): void {
    this.store.dispatch(BookingActions.loadMyBookingsWithSnapshots());
  }
}
