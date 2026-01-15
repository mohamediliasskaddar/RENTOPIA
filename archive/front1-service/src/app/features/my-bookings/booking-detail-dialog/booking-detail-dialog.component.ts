// src/app/features/my-bookings/booking-detail-dialog/booking-detail-dialog.component.ts

import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { BookingWithSnapshot } from '../../../core/models/booking-with-snapshot.model';
import { ReservationStatus, getStatusLabel, getStatusColor } from '../../../core/models/booking.model';
import * as BookingActions from '../../../store/booking/booking.actions';
import { selectCurrentBookingWithSnapshot, selectBookingLoading } from '../../../store/booking/booking.selectors';
import {selectCurrentUser} from "../../../store/auth/auth.selectors";
import {filter, take} from "rxjs/operators";
import {Router} from "@angular/router";

import * as MessagingActions from '../../../store/messaging/messaging.actions';
import * as MessagingSelectors from '../../../store/messaging/messaging.selectors';
import {EthPricePipe} from "../../../core/pipes/eth-price.pipe";

@Component({
  selector: 'app-booking-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    EthPricePipe
  ],
  templateUrl: './booking-detail-dialog.component.html',
  styleUrl: './booking-detail-dialog.component.scss'
})
export class BookingDetailDialogComponent implements OnInit {

  private store = inject(Store);
  private router = inject(Router);
  booking$: Observable<BookingWithSnapshot | null>;
  loading$: Observable<boolean>;

  constructor(
    public dialogRef: MatDialogRef<BookingDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { bookingId: number; booking?: BookingWithSnapshot },

  ) {
    console.log('🎨 BookingDetailDialog - Constructor', this.data);
    this.booking$ = this.store.select(selectCurrentBookingWithSnapshot);
    this.loading$ = this.store.select(selectBookingLoading);
  }



  ngOnInit(): void {
    console.log('🎨 BookingDetailDialog - OnInit', {
      bookingId: this.data.bookingId,
      hasBooking: !!this.data.booking
    });

    // Si le booking n'est pas passé en data, le charger
    if (!this.data.booking) {
      console.log('📥 Loading booking from store...');
      this.store.dispatch(BookingActions.loadBookingWithSnapshot({
        id: this.data.bookingId
      }));
    } else {
      console.log('✅ Booking already provided:', this.data.booking);
    }
  }

  get booking(): BookingWithSnapshot | undefined {
    return this.data.booking;
  }

  getStatusLabel(status: ReservationStatus): string {
    return getStatusLabel(status);
  }

  getStatusColor(status: ReservationStatus): string {
    return getStatusColor(status);
  }

  getMainPhoto(booking: BookingWithSnapshot): string {
    const photos = booking.propertySnapshot.photos;
    if (photos && photos.length > 0) {
      const cover = photos.find(p => p.isCover);
      return cover ? cover.photoUrl : photos[0].photoUrl;
    }
    return '';
  }

  getNights(booking: BookingWithSnapshot): number {
    const checkIn = new Date(booking.checkIn);
    const checkOut = new Date(booking.checkOut);
    return Math.ceil((checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24));
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: 'numeric'
    });
  }

  formatTime(time: string | undefined): string {
    if (!time) return 'Not specified';
    return time;
  }

  canCancel(booking: BookingWithSnapshot): boolean {
    return (booking.status === ReservationStatus.CONFIRMED ||
        booking.status === ReservationStatus.PENDING) &&
      new Date(booking.checkIn) > new Date();
  }

  onCancel(): void {
    if (confirm('Are you sure you want to cancel this booking?')) {
      this.store.dispatch(BookingActions.cancelBooking({
        id: this.data.bookingId,
        reason: 'Cancelled by user'
      }));
      this.dialogRef.close('cancelled');
    }
  }

  onClose(): void {
    console.log('🔒 Closing dialog');
    this.dialogRef.close();
  }

  openChat(): void {
    const booking = this.data.booking;

    this.store.select(selectCurrentUser).pipe(take(1)).subscribe(user => {
      if (!user) return;

      // Créer ou ouvrir conversation
      this.store.dispatch(MessagingActions.createConversation({
        request: {
          reservationId: booking!.bookingId,
          tenantId: user.id,
          hostId: booking!.hostId
        }
      }));

      // Attendre et rediriger
      this.store.select(MessagingSelectors.selectCurrentConversation)
        .pipe(
          filter(conv => conv !== null),
          take(1)
        )
        .subscribe(conversation => {
          this.dialogRef.close();
          this.router.navigate(['/messages', conversation!.id]);
        });
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
