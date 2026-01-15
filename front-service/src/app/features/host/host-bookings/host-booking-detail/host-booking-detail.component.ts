// src/app/features/host/host-booking-detail/host-booking-detail.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BookingService } from '../../../../core/services/booking.service';
import { PropertyService } from '../../../../core/services/property.service';
import { UserService } from '../../../../core/services/user.service';
import { Booking, ReservationStatus, getStatusLabel, getStatusColor } from '../../../../core/models/booking.model';
import { PropertyDetail } from '../../../../core/models/property-detail.model';
import { UserResponseDTO } from '../../../../core/models/user.model';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-host-booking-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule
  ],
  templateUrl: './host-booking-detail.component.html',
  styleUrl: './host-booking-detail.component.scss'
})
export class HostBookingDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private bookingService = inject(BookingService);
  private propertyService = inject(PropertyService);
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  // Data
  bookingId!: number;
  booking: Booking | null = null;
  property: PropertyDetail | null = null;
  guest: UserResponseDTO | null = null;

  // State
  loading = true;
  error: string | null = null;
  actionLoading = false;

  // Helpers
  getStatusLabel = getStatusLabel;
  getStatusColor = getStatusColor;
  ReservationStatus = ReservationStatus;

  ngOnInit(): void {
    this.bookingId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.bookingId) {
      this.loadBookingDetails();
    } else {
      this.error = 'Invalid booking ID';
      this.loading = false;
    }
  }

  /**
   * Load booking with property and guest details
   */
  loadBookingDetails(): void {
    this.loading = true;
    this.error = null;

    this.bookingService.getBookingById(this.bookingId).pipe(
      switchMap(booking => {
        this.booking = booking;

        // Load property and guest in parallel
        const property$ = this.propertyService.getPropertyDetails(booking.propertyId).pipe(
          catchError(() => of(null))
        );

        const guest$ = this.userService.getUserById(booking.userId).pipe(
          catchError(() => of(null))
        );

        return forkJoin({ property: property$, guest: guest$ });
      }),
      catchError(error => {
        console.error('Error loading booking:', error);
        this.error = 'Failed to load booking details';
        return of({ property: null, guest: null });
      })
    ).subscribe(({ property, guest }) => {
      this.property = property;
      this.guest = guest;
      this.loading = false;
    });
  }

  /**
   * Go back to bookings list
   */
  goBack(): void {
    this.router.navigate(['/host/bookings']);
  }

  /**
   * Check-in guest
   */
  checkInGuest(): void {
    if (!this.booking || this.booking.status !== ReservationStatus.CONFIRMED) {
      return;
    }

    this.actionLoading = true;

    this.bookingService.checkIn(this.bookingId).subscribe({
      next: (updated) => {
        this.booking = updated;
        this.actionLoading = false;
        this.snackBar.open('Guest checked in successfully!', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.actionLoading = false;
        console.error('Check-in error:', err);
        this.snackBar.open(err.error?.message || 'Failed to check-in guest', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Check-out guest
   */
  checkOutGuest(): void {
    if (!this.booking || this.booking.status !== ReservationStatus.CHECKED_IN) {
      return;
    }

    this.actionLoading = true;

    this.bookingService.checkOut(this.bookingId).subscribe({
      next: (updated) => {
        this.booking = updated;
        this.actionLoading = false;
        this.snackBar.open('Guest checked out successfully!', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.actionLoading = false;
        console.error('Check-out error:', err);
        this.snackBar.open(err.error?.message || 'Failed to check-out guest', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Cancel reservation
   */
  cancelReservation(): void {
    if (!this.booking) return;

    const reason = prompt('Reason for cancellation:');
    if (!reason) return;

    this.actionLoading = true;

    this.bookingService.cancelBooking(this.bookingId, reason).subscribe({
      next: (updated) => {
        this.booking = updated;
        this.actionLoading = false;
        this.snackBar.open('Reservation cancelled', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.actionLoading = false;
        console.error('Cancel error:', err);
        this.snackBar.open(err.error?.message || 'Failed to cancel reservation', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Format date
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  /**
   * Format time
   */
  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Calculate nights
   */
  calculateNights(): number {
    if (!this.booking) return 0;
    const start = new Date(this.booking.checkInDate);
    const end = new Date(this.booking.checkOutDate);
    return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
  }

  /**
   * Get photo URL
   */
  getPhotoUrl(url: string | undefined): string {
    if (!url) return 'assets/images/default-avatar.png';
    if (url.startsWith('http')) return url;
    return `http://localhost:8087/api/media/files/${url}`;
  }

  /**
   * Get property cover photo
   */
  getPropertyCover(): string {
    if (!this.property?.photos?.length) return '';
    const cover = this.property.photos.find(p => p.isCover) || this.property.photos[0];
    return this.getPhotoUrl(cover.photoUrl);
  }

  /**
   * Can check-in
   */
  canCheckIn(): boolean {
    if (!this.booking) return false;
    return this.booking.status === ReservationStatus.CONFIRMED;
  }

  /**
   * Can check-out
   */
  canCheckOut(): boolean {
    if (!this.booking) return false;
    return this.booking.status === ReservationStatus.CHECKED_IN;
  }

  /**
   * Can cancel
   */
  canCancel(): boolean {
    if (!this.booking) return false;
    return this.booking.status === ReservationStatus.PENDING ||
      this.booking.status === ReservationStatus.CONFIRMED;
  }

  /**
   * View property
   */
  viewProperty(): void {
    if (this.property) {
      this.router.navigate(['/host/properties', this.property.propertyId]);
    }
  }
}
