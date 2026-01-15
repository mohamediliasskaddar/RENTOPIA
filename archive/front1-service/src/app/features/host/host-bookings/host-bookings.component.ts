// src/app/features/host/host-bookings/host-bookings.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { Store } from '@ngrx/store';

import { BookingService } from '../../../core/services/booking.service';
import { PropertyService } from '../../../core/services/property.service';
import { UserService } from '../../../core/services/user.service';
import { Booking, ReservationStatus, getStatusLabel, getStatusColor } from '../../../core/models/booking.model';
import { HostBooking, HostBookingStats } from '../../../core/models/host-booking.model';
import { selectUserId } from '../../../store/auth/auth.selectors'; // ✅ Importer le sélecteur
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap, take } from 'rxjs/operators';
import {EthPricePipe} from "../../../core/pipes/eth-price.pipe";

@Component({
  selector: 'app-host-bookings',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatTooltipModule,
    MatBadgeModule,
    EthPricePipe
  ],
  templateUrl: './host-bookings.component.html',
  styleUrl: './host-bookings.component.scss'
})
export class HostBookingsComponent implements OnInit {
  private bookingService = inject(BookingService);
  private propertyService = inject(PropertyService);
  private userService = inject(UserService);
  private store = inject(Store);

  // User
  userId: number | null = null;

  // Data
  bookings: HostBooking[] = [];
  filteredBookings: HostBooking[] = [];
  stats: HostBookingStats = {
    total: 0,
    pending: 0,
    confirmed: 0,
    checkedIn: 0,
    completed: 0,
    cancelled: 0,
    totalRevenue: 0,
    upcomingRevenue: 0
  };

  // Filters
  selectedStatus: string = 'all';
  selectedPropertyId: number | null = null;
  properties: { propertyId: number; title: string }[] = [];

  // State
  loading = true;
  error: string | null = null;

  // Tab index
  selectedTabIndex = 0;

  // Helpers
  getStatusLabel = getStatusLabel;
  getStatusColor = getStatusColor;
  ReservationStatus = ReservationStatus;

  ngOnInit(): void {
    // ✅ Récupérer le userId depuis le store
    this.store.select(selectUserId).pipe(take(1)).subscribe(userId => {
      this.userId = userId;
      if (this.userId) {
        this.loadBookings(); // ✅ Les propriétés sont chargées dans loadBookings()
      } else {
        this.error = 'User not authenticated';
        this.loading = false;
      }
    });
  }

  /**
   * Load all bookings for the host
   */
  loadBookings(): void {
    if (!this.userId) return;

    this.loading = true;
    this.error = null;

    // 1. D'abord récupérer les propriétés du host
    this.propertyService.getMyProperties(this.userId).pipe(
      switchMap(properties => {
        if (properties.length === 0) {
          return of([]);
        }

        // Stocker les propriétés pour le filtre
        this.properties = properties.map(p => ({
          propertyId: p.propertyId,
          title: p.title
        }));

        // 2. Pour chaque propriété, récupérer ses bookings
        const bookingRequests = properties.map(prop =>
          this.bookingService.getPropertyBookings(prop.propertyId).pipe(
            catchError(err => {
              console.warn(`Failed to load bookings for property ${prop.propertyId}:`, err);
              return of([]);
            })
          )
        );

        return forkJoin(bookingRequests).pipe(
          // Flatten all bookings into one array
          map(bookingsArrays => bookingsArrays.flat())
        );
      }),
      switchMap(bookings => {
        if (bookings.length === 0) {
          return of([]);
        }

        // 3. Enrichir chaque booking avec les infos guest et property
        const enrichedBookings$ = bookings.map(booking =>
          this.enrichBooking(booking)
        );

        return forkJoin(enrichedBookings$);
      }),
      catchError(error => {
        console.error('Error loading bookings:', error);
        this.error = 'Failed to load reservations';
        return of([]);
      })
    ).subscribe(enrichedBookings => {
      this.bookings = enrichedBookings;
      this.calculateStats();
      this.applyFilters();
      this.loading = false;
    });
  }


  /**
   * Enrich booking with guest and property info
   */
  private enrichBooking(booking: Booking): import('rxjs').Observable<HostBooking> {
    const hostBooking: HostBooking = { ...booking };

    // Fetch guest info
    const guest$ = this.userService.getUserById(booking.userId).pipe(
      map(user => ({
        id: user.id,
        fullName: `${user.prenom} ${user.nom}`,
        email: user.email,
        photoUrl: user.photoUrl,
        phone: user.tel
      })),
      catchError(() => of(undefined))
    );

    // Fetch property info
    const property$ = this.propertyService.getPropertyDetails(booking.propertyId).pipe(
      map(prop => ({
        propertyId: prop.propertyId,
        title: prop.title,
        city: prop.city,
        country: prop.country,
        coverPhoto: prop.photos?.find(p => p.isCover)?.photoUrl || prop.photos?.[0]?.photoUrl
      })),
      catchError(() => of(undefined))
    );

    return forkJoin({ guest: guest$, property: property$ }).pipe(
      map(({ guest, property }) => ({
        ...hostBooking,
        guest,
        property
      }))
    );
  }

  /**
   * Load host properties for filter
   */
  loadProperties(): void {
    if (!this.userId) return;

    // ✅ Utiliser getMyProperties avec userId
    this.propertyService.getMyProperties(this.userId).subscribe({
      next: (properties) => {
        this.properties = properties.map(p => ({
          propertyId: p.propertyId,
          title: p.title
        }));
      },
      error: (err) => console.error('Error loading properties:', err)
    });
  }

  /**
   * Calculate stats
   */
  calculateStats(): void {
    const now = new Date();

    this.stats = {
      total: this.bookings.length,
      pending: this.bookings.filter(b => b.status === ReservationStatus.PENDING).length,
      confirmed: this.bookings.filter(b => b.status === ReservationStatus.CONFIRMED).length,
      checkedIn: this.bookings.filter(b => b.status === ReservationStatus.CHECKED_IN).length,
      completed: this.bookings.filter(b => b.status === ReservationStatus.COMPLETED).length,
      cancelled: this.bookings.filter(b => b.status === ReservationStatus.CANCELLED).length,
      totalRevenue: this.bookings
        .filter(b => b.status === ReservationStatus.COMPLETED)
        .reduce((sum, b) => sum + b.priceBreakdown.totalAmount, 0),
      upcomingRevenue: this.bookings
        .filter(b =>
          (b.status === ReservationStatus.CONFIRMED || b.status === ReservationStatus.PENDING) &&
          new Date(b.checkInDate) > now
        )
        .reduce((sum, b) => sum + b.priceBreakdown.totalAmount, 0)
    };
  }

  /**
   * Apply filters
   */
  applyFilters(): void {
    let filtered = [...this.bookings];

    // Filter by status
    if (this.selectedStatus !== 'all') {
      filtered = filtered.filter(b => b.status === this.selectedStatus);
    }

    // Filter by property
    if (this.selectedPropertyId) {
      filtered = filtered.filter(b => b.propertyId === this.selectedPropertyId);
    }

    // Sort by date (newest first)
    filtered.sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    this.filteredBookings = filtered;
  }

  /**
   * Get upcoming bookings
   */
  getUpcomingBookings(): HostBooking[] {
    const now = new Date();
    return this.bookings
      .filter(b =>
        new Date(b.checkInDate) > now &&
        (b.status === ReservationStatus.CONFIRMED || b.status === ReservationStatus.PENDING)
      )
      .sort((a, b) => new Date(a.checkInDate).getTime() - new Date(b.checkInDate).getTime());
  }

  /**
   * Get current bookings (checked in)
   */
  getCurrentBookings(): HostBooking[] {
    return this.bookings.filter(b => b.status === ReservationStatus.CHECKED_IN);
  }

  /**
   * Get past bookings
   */
  getPastBookings(): HostBooking[] {
    return this.bookings
      .filter(b => b.status === ReservationStatus.COMPLETED)
      .sort((a, b) => new Date(b.checkOutDate).getTime() - new Date(a.checkOutDate).getTime());
  }

  /**
   * Format date
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
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
   * Calculate nights
   */
  calculateNights(checkIn: string, checkOut: string): number {
    const start = new Date(checkIn);
    const end = new Date(checkOut);
    return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
  }

  /**
   * Tab changed
   */
  onTabChange(index: number): void {
    this.selectedTabIndex = index;
  }
}
