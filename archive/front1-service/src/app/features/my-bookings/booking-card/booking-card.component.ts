// src/app/features/my-bookings/components/booking-card/booking-card.component.ts

import {Component, Input, Output, EventEmitter, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { BookingWithSnapshot } from '../../../core/models/booking-with-snapshot.model';
import { ReservationStatus, getStatusLabel, getStatusColor } from '../../../core/models/booking.model';
import { Store } from '@ngrx/store';
import { selectCurrentUser } from '../../../store/auth/auth.selectors';
import {filter, take, timeout, catchError} from 'rxjs/operators';
import {Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import { ReviewFormComponent } from '../../review-form/review-form.component';
import {ReviewViewComponent} from "../review-view/review-view.component";
import { of } from 'rxjs';

// ✅ NOUVEAU: Import messaging
import * as MessagingActions from '../../../store/messaging/messaging.actions';
import * as MessagingSelectors from '../../../store/messaging/messaging.selectors';
import {EthPricePipe} from "../../../core/pipes/eth-price.pipe";

@Component({
  selector: 'app-booking-card',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    EthPricePipe
  ],
  templateUrl: './booking-card.component.html',
  styleUrl: './booking-card.component.scss'
})
export class BookingCardComponent {

  @Input() booking!: BookingWithSnapshot;
  @Output() viewDetails = new EventEmitter<number>();
  @Output() cancel = new EventEmitter<number>();
  @Output() viewProperty = new EventEmitter<number>();
  @Output() reviewSubmitted = new EventEmitter<void>();

  private dialog = inject(MatDialog);
  private store = inject(Store);
  private router = inject(Router);

  /**
   * ✅ SOLUTION ROBUSTE: Ouvrir ou créer une conversation
   */
  onMessageHost(): void {
    console.log('💬 Opening chat for booking:', this.booking.bookingId);

    this.store.select(selectCurrentUser).pipe(take(1)).subscribe(user => {
      if (!user) {
        console.error('❌ User not authenticated');
        return;
      }

      // 1️⃣ Reset current conversation (important!)
      this.store.dispatch(MessagingActions.clearCurrentConversation());

      // 2️⃣ Charger la conversation
      this.store.dispatch(
        MessagingActions.loadConversationByReservation({
          reservationId: this.booking.bookingId
        })
      );

      // 3️⃣ Écouter le résultat avec timeout
      this.store.select(MessagingSelectors.selectCurrentConversation)
        .pipe(
          filter(conv => conv !== undefined), // Attendre qu'il y ait une valeur (null ou object)
          take(1),
          timeout(5000), // Timeout de 5 secondes
          catchError(error => {
            console.error('⏱️ Timeout or error loading conversation:', error);
            return of(null);
          })
        )
        .subscribe(conv => {
          console.log('📊 Conversation result:', conv);

          if (conv) {
            // ✅ Conversation existante trouvée
            console.log('✅ Conversation found, navigating to:', conv.id);
            this.router.navigate(['/messages', conv.id]);
          } else {
            // ➕ Pas de conversation, en créer une
            console.log('📝 No conversation found, creating new one...');
            this.createConversation(user.id);
          }
        });
    });
  }

  /**
   * ✅ CORRIGÉ: Créer une conversation
   */
  private createConversation(currentUserId: number): void {
    console.log('🔨 Creating conversation for reservation:', this.booking.bookingId);

    // 1️⃣ Dispatch l'action de création
    this.store.dispatch(
      MessagingActions.createConversation({
        request: {
          reservationId: this.booking.bookingId,
          tenantId: currentUserId,
          hostId: this.booking.hostId
        }
      })
    );

    // 2️⃣ Attendre que la conversation soit créée et définie
    this.store.select(MessagingSelectors.selectCurrentConversation)
      .pipe(
        filter(conv => !!conv && conv.reservationId === this.booking.bookingId),
        take(1),
        timeout(5000),
        catchError(error => {
          console.error('⏱️ Timeout or error creating conversation:', error);
          alert('Impossible de créer la conversation. Veuillez réessayer.');
          return of(null);
        })
      )
      .subscribe(conv => {
        if (conv) {
          console.log('✅ Conversation created successfully, navigating to:', conv.id);
          this.router.navigate(['/messages', conv.id]);
        }
      });
  }

  /**
   * ✅ NOUVEAU: Vérifier si le chat est disponible
   */
  get canChat(): boolean {
    return this.booking.status === ReservationStatus.CONFIRMED ||
      this.booking.status === ReservationStatus.PENDING ||
      this.booking.status === ReservationStatus.COMPLETED;
  }

  get mainPhoto(): string {
    const photos = this.booking.propertySnapshot.photos;
    if (photos && photos.length > 0) {
      const coverPhoto = photos.find(p => p.isCover);
      return coverPhoto ? coverPhoto.photoUrl : photos[0].photoUrl;
    }
    return '';
  }

  get checkInDate(): Date {
    return new Date(this.booking.checkIn);
  }

  get checkOutDate(): Date {
    return new Date(this.booking.checkOut);
  }

  get nights(): number {
    const diff = this.checkOutDate.getTime() - this.checkInDate.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  get statusLabel(): string {
    return getStatusLabel(this.booking.status);
  }

  get statusColor(): string {
    return getStatusColor(this.booking.status);
  }

  get canCancel(): boolean {
    return this.booking.status === ReservationStatus.CONFIRMED ||
      this.booking.status === ReservationStatus.PENDING;
  }

  get isUpcoming(): boolean {
    return new Date(this.booking.checkIn) > new Date();
  }

  formatDate(date: Date): string {
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  onViewDetails(): void {
    this.viewDetails.emit(this.booking.bookingId);
  }

  onCancel(): void {
    if (confirm('Are you sure you want to cancel this booking?')) {
      this.cancel.emit(this.booking.bookingId);
    }
  }

  onViewProperty(): void {
    this.viewProperty.emit(this.booking.propertyId);
  }

  get canReview(): boolean {
    return this.booking.userCanReview || false;
  }

  get hasReviewed(): boolean {
    return this.booking.hasReview || false;
  }

  onLeaveReview(): void {
    this.store.select(selectCurrentUser).pipe(take(1)).subscribe(user => {
      if (!user) {
        console.error('User not authenticated');
        return;
      }

      const dialogRef = this.dialog.open(ReviewFormComponent, {
        width: '600px',
        maxWidth: '90vw',
        maxHeight: '90vh',
        panelClass: 'review-form-dialog-container',
        backdropClass: 'review-form-dialog-backdrop',
        hasBackdrop: true,
        disableClose: false,
        autoFocus: true,
        data: {
          booking: this.booking,
          userId: user.id
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.reviewSubmitted.emit();
        }
      });
    });
  }

  onViewReview(): void {
    this.dialog.open(ReviewViewComponent, {
      width: '600px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      panelClass: 'review-view-dialog-container',
      backdropClass: 'review-view-dialog-backdrop',
      hasBackdrop: true,
      disableClose: false,
      autoFocus: false,
      data: {
        review: this.booking.review
      }
    });
  }

  onEditReview(): void {
    const dialogRef = this.dialog.open(ReviewFormComponent, {
      width: '600px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      panelClass: 'review-form-dialog-container',
      backdropClass: 'review-form-dialog-backdrop',
      hasBackdrop: true,
      disableClose: false,
      autoFocus: false,
      data: {
        review: this.booking.review
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('Review updated successfully');
        this.reviewSubmitted.emit();
      }
    });
  }


}
