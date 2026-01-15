// src/app/features/property-detail/booking-card/booking-card.component.ts

import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil, combineLatest, filter, take } from 'rxjs';
import { Store } from '@ngrx/store';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Material
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

// Models
import { PropertyDetail } from '../../../core/models/property-detail.model';
import { CreateBookingDTO } from '../../../core/models/booking.model';
import { User } from '../../../core/models/user.model';

// Store
import * as BookingActions from '../../../store/booking/booking.actions';
import * as PaymentActions from '../../../store/payment/payment.actions';
import * as ListingsActions from '../../../store/listings/listing.actions';
import {
  selectBookingLoading,
  selectCurrentBooking,
  selectBookingError
} from '../../../store/booking/booking.selectors';
import {
  selectPropertyBlockedDates,
  selectListingsLoading
} from '../../../store/listings/listing.selectors';
import { selectCurrentUser } from '../../../store/auth/auth.selectors';

// Components
import { PaymentModalComponent, PaymentModalData } from '../payment-modal/payment-modal.component';
import { MatCheckbox } from "@angular/material/checkbox";
import { EthPricePipe } from "../../../core/pipes/eth-price.pipe";

@Component({
  selector: 'app-booking-card',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatCheckbox,
    EthPricePipe
  ],
  templateUrl: './booking-card.component.html',
  styleUrl: './booking-card.component.scss'
})
export class BookingCardComponent implements OnInit, OnDestroy {

  @Input() property!: PropertyDetail;
  @Input() propertyId!: number;

  // Form
  bookingForm!: FormGroup;

  // User
  currentUser: User | null = null;

  // Disponibilité
  blockedDates: Set<string> = new Set();

  // Prix (toujours en ETH)
  totalNights = 0;
  baseAmount = 0;
  cleaningFee = 0;
  petFee = 0;
  serviceFee = 0;
  totalAmount = 0;

  // UI State
  loading = false;
  error: string | null = null;

  // Date limits
  minDate = new Date();
  maxDate = new Date(new Date().setMonth(new Date().getMonth() + 12));

  // ✅ NOUVEAU : Pour stocker les données de réservation
  private pendingCheckIn: Date | null = null;
  private pendingCheckOut: Date | null = null;
  private pendingNumGuests: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private dialog: MatDialog,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.subscribeToStore();
    this.loadBlockedDates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initForm(): void {
    this.bookingForm = this.fb.group({
      checkIn: [null, Validators.required],
      checkOut: [null, Validators.required],
      numGuests: [1, [Validators.required, Validators.min(1), Validators.max(this.property.maxGuests)]],
      hasPets: [false]
    });

    this.bookingForm.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.calculatePrice());
  }

  private subscribeToStore(): void {
    this.store.select(selectCurrentUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => this.currentUser = user);

    this.store.select(selectPropertyBlockedDates)
      .pipe(takeUntil(this.destroy$))
      .subscribe(dates => {
        this.blockedDates = new Set(dates);
      });

    combineLatest([
      this.store.select(selectListingsLoading),
      this.store.select(selectBookingLoading)
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([listingsLoading, bookingLoading]) => {
        this.loading = listingsLoading || bookingLoading;
      });

    // ✅ NOUVEAU : Écouter la création de réservation pour ouvrir automatiquement la modal de paiement
    this.store.select(selectCurrentBooking)
      .pipe(
        takeUntil(this.destroy$),
        filter(booking => booking !== null && booking.status === 'PENDING')
      )
      .subscribe(booking => {
        console.log('✅ Réservation créée (PENDING), ouverture de la modal de paiement...');

        // Afficher un message de succès
        this.snackBar.open(
          '✅ Réservation créée ! Veuillez procéder au paiement.',
          'OK',
          { duration: 5000, panelClass: ['success-snackbar'] }
        );

        // Ouvrir automatiquement la modal de paiement
        this.openPaymentModal(booking!.id);
      });

    this.store.select(selectBookingError)
      .pipe(
        takeUntil(this.destroy$),
        filter(error => error !== null)
      )
      .subscribe(error => {
        this.error = error;
        console.error('❌ Booking error:', error);
        this.snackBar.open(`❌ ${error}`, 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      });
  }

  private loadBlockedDates(): void {
    const today = new Date();
    const oneYearLater = new Date();
    oneYearLater.setFullYear(oneYearLater.getFullYear() + 1);

    const start = this.formatDate(today);
    const end = this.formatDate(oneYearLater);

    this.store.dispatch(ListingsActions.loadPropertyBlockedDates({
      propertyId: this.propertyId,
      start,
      end
    }));
  }

  dateFilter = (date: Date | null): boolean => {
    if (!date) return false;

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (date < today) return false;

    const dateStr = this.formatDate(date);
    if (this.blockedDates.has(dateStr)) {
      return false;
    }

    return true;
  };

  checkOutFilter = (date: Date | null): boolean => {
    if (!date) return false;

    const checkIn = this.bookingForm.get('checkIn')?.value;
    if (!checkIn) return this.dateFilter(date);

    const checkInDate = new Date(checkIn);
    const diffTime = date.getTime() - checkInDate.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (this.property.minStayNights && diffDays < this.property.minStayNights) {
      return false;
    }

    if (this.property.maxStayNights && diffDays > this.property.maxStayNights) {
      return false;
    }

    const current = new Date(checkInDate);
    while (current < date) {
      const dateStr = this.formatDate(current);
      if (this.blockedDates.has(dateStr)) {
        return false;
      }
      current.setDate(current.getDate() + 1);
    }

    return this.dateFilter(date);
  };

  private calculatePrice(): void {
    const checkIn = this.bookingForm.get('checkIn')?.value;
    const checkOut = this.bookingForm.get('checkOut')?.value;
    const hasPets = this.bookingForm.get('hasPets')?.value;

    if (!checkIn || !checkOut) {
      this.resetPrice();
      return;
    }

    const diffTime = checkOut.getTime() - checkIn.getTime();
    this.totalNights = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (this.totalNights <= 0) {
      this.resetPrice();
      return;
    }

    this.baseAmount = this.property.pricePerNight * this.totalNights;
    this.cleaningFee = this.property.cleaningFee || 0;
    this.petFee = hasPets && this.property.petFee ? this.property.petFee : 0;
    this.serviceFee = (this.baseAmount + this.cleaningFee + this.petFee) * 0.10;
    this.totalAmount = this.baseAmount + this.cleaningFee + this.petFee + this.serviceFee;
  }

  private resetPrice(): void {
    this.totalNights = 0;
    this.baseAmount = 0;
    this.cleaningFee = 0;
    this.petFee = 0;
    this.serviceFee = 0;
    this.totalAmount = 0;
  }

  /**
   * ✅ MODIFIÉ : Créer la réservation (status PENDING)
   * La modal de paiement s'ouvrira automatiquement après
   */
  onReserve(): void {
    const checkIn = this.bookingForm.get('checkIn')?.value;
    const checkOut = this.bookingForm.get('checkOut')?.value;
    const numGuests = this.bookingForm.get('numGuests')?.value;
    const hasPets = this.bookingForm.get('hasPets')?.value;

    if (!checkIn || !checkOut) {
      this.error = 'Veuillez sélectionner les dates de check-in et check-out';
      this.snackBar.open('❌ Veuillez sélectionner les dates', 'Fermer', {
        duration: 3000
      });
      return;
    }

    if (!this.currentUser) {
      this.router.navigate(['login'], {
        queryParams: { returnUrl: this.router.url }
      });
      return;
    }

    // ✅ Sauvegarder les données pour la modal
    this.pendingCheckIn = checkIn;
    this.pendingCheckOut = checkOut;
    this.pendingNumGuests = numGuests;

    // ✅ Créer la réservation (PENDING)
    const createBookingDTO: CreateBookingDTO = {
      propertyId: this.propertyId,
      checkInDate: this.toLocalDateTime(checkIn),
      checkOutDate: this.toLocalDateTimeCheckOut(checkOut),
      numGuests: numGuests,
      hasPets: hasPets || false
    };

    console.log('🔥 Création de la réservation...');
    this.store.dispatch(BookingActions.createBooking({
      booking: createBookingDTO
    }));
  }

  /**
   * ✅ NOUVEAU : Ouvrir la modal de paiement
   */
  private openPaymentModal(reservationId: number): void {
    if (!this.pendingCheckIn || !this.pendingCheckOut || !this.pendingNumGuests) {
      console.error('❌ Données de réservation manquantes');
      return;
    }

    const dialogData: PaymentModalData = {
      property: this.property,
      totalAmount: this.totalAmount,
      totalNights: this.totalNights,
      checkIn: this.pendingCheckIn,
      checkOut: this.pendingCheckOut,
      numGuests: this.pendingNumGuests
    };

    const dialogRef = this.dialog.open(PaymentModalComponent, {
      width: '600px',
      maxWidth: '95vw',
      data: dialogData,
      disableClose: true, // Ne peut pas fermer en cliquant à l'extérieur
      panelClass: 'payment-dialog-centered'
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('💳 Modal de paiement fermée:', result);

      if (result?.success) {
        // Paiement réussi
        this.snackBar.open(
          '✅ Paiement confirmé ! Votre réservation est validée.',
          'OK',
          { duration: 5000, panelClass: ['success-snackbar'] }
        );

        // Rediriger vers la page de confirmation
        this.router.navigate(['/bookings', result.reservationId]);
      } else {
        // Paiement annulé ou échoué
        this.snackBar.open(
          '⚠️ Paiement non effectué. Votre réservation reste en attente.',
          'OK',
          { duration: 5000, panelClass: ['warning-snackbar'] }
        );
      }

      // Réinitialiser les données temporaires
      this.pendingCheckIn = null;
      this.pendingCheckOut = null;
      this.pendingNumGuests = null;
    });
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private toLocalDateTime(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}T14:00:00`;
  }

  private toLocalDateTimeCheckOut(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}T11:00:00`;
  }

  // Getters
  get hasMinStay(): boolean {
    return !!this.property.minStayNights && this.property.minStayNights > 1;
  }

  get hasMaxStay(): boolean {
    return !!this.property.maxStayNights;
  }

  get canReserve(): boolean {
    return this.bookingForm.valid &&
      this.totalAmount > 0 &&
      !this.loading;
  }

  get isAuthenticated(): boolean {
    return this.currentUser !== null;
  }

  get pricePerNight(): number {
    return this.property.pricePerNight;
  }

  get hasWeekendPrice(): boolean {
    return !!this.property.weekendPricePerNight &&
      this.property.weekendPricePerNight !== this.property.pricePerNight;
  }

  get weekendPrice(): number | null {
    return this.property.weekendPricePerNight || null;
  }
}
