// src/app/features/property-detail/payment-modal/payment-modal.component.ts

import { Component, Inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { Subject, takeUntil, combineLatest, filter } from 'rxjs';
import { Store } from '@ngrx/store';

// Material
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatStepperModule } from '@angular/material/stepper';
import { MatDividerModule } from '@angular/material/divider';

// Models
import { PropertyDetail } from '../../../core/models/property-detail.model';
import { SignedTransactionRequest } from '../../../core/models/payment.model';

// Store
import * as PaymentActions from '../../../store/payment/payment.actions';
import * as BookingActions from '../../../store/booking/booking.actions';
import {
  selectIsWalletConnected,
  selectWalletAddress,
  selectHasSufficientBalance,
  selectCurrentTransaction,
  selectIsPolling,
  selectPollingProgress,
  selectPaymentError,
  selectIsTransactionConfirmed
} from '../../../store/payment/payment.selectors';
import {
  selectCurrentBooking,
  selectBookingError
} from '../../../store/booking/booking.selectors';

// Services
import { Web3Service } from '../../../core/services/web3.service';
import { UserService } from '../../../core/services/user.service';
import { UserResponseDTO } from '../../../core/models/user.model';

export interface PaymentModalData {
  property: PropertyDetail;
  totalAmount: number; // ✅ EN ETH (ex: 0.044)
  totalNights: number;
  checkIn: Date;
  checkOut: Date;
  numGuests: number;
  reservationId?: number;
}

@Component({
  selector: 'app-payment-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatStepperModule,
    MatDividerModule
  ],
  templateUrl: './payment-modal.component.html',
  styleUrl: './payment-modal.component.scss'
})
export class PaymentModalComponent implements OnInit, OnDestroy {

  // États du processus
  currentStep = 0;
  isWalletConnected = false;
  walletAddress: string | null = null;
  hasSufficientBalance: boolean | null = null;

  // Transaction
  txHash: string | null = null;
  reservationId: number | null = null;
  isPolling = false;
  pollingProgress = 0;
  isConfirmed = false;

  // Erreurs
  error: string | null = null;

  // Loading
  loading = false;

  private destroy$ = new Subject<void>();
  ownerWalletAddress: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<PaymentModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PaymentModalData,
    private store: Store,
    private web3Service: Web3Service,
    private userService: UserService
  ) {
    // ✅ LOG pour vérifier les données reçues
    console.log('📦 Payment Modal Data:', {
      totalAmount: this.data.totalAmount,
      totalAmountEth: this.totalAmountEth,
      property: this.data.property.title,
      nights: this.data.totalNights
    });
  }

  ngOnInit(): void {
    if (this.data.reservationId) {
      this.reservationId = this.data.reservationId;
      console.log('✅ ReservationId reçu:', this.reservationId);
    }
    this.loadOwnerWallet();
    this.initPaymentSteps();
    this.subscribeToStore();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    if (this.isPolling) {
      this.store.dispatch(PaymentActions.stopPolling());
    }
  }

  private initPaymentSteps(): void {
    this.store.dispatch(PaymentActions.initPaymentSteps());
  }

  private loadOwnerWallet(): void {
    const ownerId = this.data.property.userId;

    this.userService.getUserById(ownerId).subscribe({
      next: (user: UserResponseDTO) => {
        if (!user.walletAdresse) {
          this.error = 'Le propriétaire n\'a pas de wallet configuré';
          return;
        }

        this.ownerWalletAddress = user.walletAdresse;
        console.log('✅ Wallet propriétaire:', this.ownerWalletAddress);
      },
      error: () => {
        this.error = 'Impossible de récupérer le wallet du propriétaire';
      }
    });
  }

  private subscribeToStore(): void {
    // Wallet
    combineLatest([
      this.store.select(selectIsWalletConnected),
      this.store.select(selectWalletAddress)
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([connected, address]) => {
        this.isWalletConnected = connected;
        this.walletAddress = address;

        if (connected) {
          this.currentStep = Math.max(this.currentStep, 1);
          this.updateStep(1, 'completed');
        }
      });

    // Solde
    this.store.select(selectHasSufficientBalance)
      .pipe(takeUntil(this.destroy$))
      .subscribe(sufficient => {
        this.hasSufficientBalance = sufficient;

        if (sufficient === true) {
          this.currentStep = Math.max(this.currentStep, 2);
          this.updateStep(2, 'completed');
        } else if (sufficient === false) {
          this.error = 'Solde insuffisant';
          this.updateStep(2, 'failed', 'Solde insuffisant');
        }
      });

    // Réservation créée
    if (!this.reservationId) {
      this.store.select(selectCurrentBooking)
        .pipe(
          filter(booking => booking !== null),
          takeUntil(this.destroy$)
        )
        .subscribe(booking => {
          if (booking) {
            this.reservationId = booking.id;
            console.log('✅ Réservation créée:', booking.id);
          }
        });
    }

    // Transaction créée
    this.store.select(selectCurrentTransaction)
      .pipe(
        filter(tx => tx !== null),
        takeUntil(this.destroy$)
      )
      .subscribe(transaction => {
        if (transaction) {
          this.txHash = transaction.transactionHash;
          this.currentStep = Math.max(this.currentStep, 4);
          this.updateStep(3, 'completed');
          console.log('✅ Transaction enregistrée:', this.txHash);

          this.startPolling();
        }
      });

    // Polling
    combineLatest([
      this.store.select(selectIsPolling),
      this.store.select(selectPollingProgress)
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([polling, progress]) => {
        this.isPolling = polling;
        this.pollingProgress = progress;

        if (polling) {
          this.updateStep(4, 'processing', `Confirmation en cours... ${progress}%`);
        }
      });

    // Confirmation on-chain
    this.store.select(selectIsTransactionConfirmed)
      .pipe(
        filter(confirmed => confirmed === true),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.isConfirmed = true;
        this.updateStep(4, 'completed');
        this.updateStep(5, 'completed');
        this.currentStep = 5;

        console.log('✅ Transaction confirmée on-chain!');

        setTimeout(() => {
          this.close(true);
        }, 2000);
      });

    // Erreurs
    combineLatest([
      this.store.select(selectPaymentError),
      this.store.select(selectBookingError)
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([paymentError, bookingError]) => {
        this.error = paymentError || bookingError;
      });
  }

  connectWallet(): void {
    console.log('🔥 Connexion MetaMask...');
    this.loading = true;
    this.updateStep(1, 'processing');

    this.store.dispatch(PaymentActions.connectWallet());

    setTimeout(() => {
      this.loading = false;
    }, 1000);
  }

  verifyBalance(): void {
    if (!this.walletAddress) {
      this.error = 'Wallet non connecté';
      return;
    }

    this.loading = true;
    this.updateStep(2, 'processing');

    console.log('🔍 Vérification du solde pour:', this.totalAmountEth, 'ETH');

    this.store.dispatch(PaymentActions.loadWalletBalance({
      walletAddress: this.walletAddress,
      requiredAmountEth: this.totalAmountEth
    }));

    setTimeout(() => this.loading = false, 1000);
  }

  async signTransaction(): Promise<void> {
    if (!this.walletAddress || !this.reservationId) {
      this.error = 'Données manquantes (wallet ou reservationId)';
      console.error('❌ Missing:', {
        walletAddress: this.walletAddress,
        reservationId: this.reservationId
      });
      return;
    }

    if (!this.ownerWalletAddress) {
      this.error = 'Adresse du propriétaire introuvable';
      console.error('❌ Owner wallet address missing');
      return;
    }

    console.log('🔥 Signature de la transaction MetaMask...');
    console.log('📊 Détails transaction:', {
      from: this.walletAddress,
      to: this.ownerWalletAddress,
      amountEth: this.totalAmountEth,
      reservationId: this.reservationId
    });

    this.loading = true;
    this.updateStep(3, 'processing');

    try {
      const txHash = await this.requestMetaMaskTransaction();

      if (!txHash) {
        throw new Error('Transaction annulée');
      }

      console.log('✅ Transaction signée:', txHash);
      this.confirmPaymentToBackend(txHash);

    } catch (error: any) {
      console.error('❌ Erreur signature:', error);
      this.error = error.message || 'Erreur lors de la signature';
      this.updateStep(3, 'failed');
      this.loading = false;
    }
  }

  /**
   * ✅ CORRECTION : Requête MetaMask avec montant correct
   */
  private async requestMetaMaskTransaction(): Promise<string | null> {
    if (!window.ethereum) {
      throw new Error('MetaMask non installé');
    }

    try {
      // ✅ Conversion ETH → Wei (hexadécimal)
      const amountInWei = this.web3Service.ethToWei(this.totalAmountEth);

      console.log('💰 Montant transaction:', {
        eth: this.totalAmountEth,
        wei: amountInWei,
        from: this.walletAddress,
        to: this.ownerWalletAddress
      });

      const transactionParameters = {
        to: this.ownerWalletAddress!,
        from: this.walletAddress!,
        value: amountInWei, // ✅ EN WEI (hexadécimal)
      };

      console.log('📤 Envoi transaction à MetaMask:', transactionParameters);

      const txHash = await window.ethereum.request({
        method: 'eth_sendTransaction',
        params: [transactionParameters],
      });

      return txHash;

    } catch (error: any) {
      if (error.code === 4001) {
        throw new Error('Transaction refusée par l\'utilisateur');
      }
      throw error;
    }
  }

  private confirmPaymentToBackend(txHash: string): void {
    if (!this.reservationId || !this.walletAddress) return;

    const request: SignedTransactionRequest = {
      reservationId: this.reservationId,
      transactionHash: txHash,
      fromAddress: this.walletAddress,
      amountEth: this.totalAmountEth, // ✅ EN ETH
      tenantId: 1 // TODO: Récupérer depuis currentUser
    };

    console.log('📤 Envoi confirmation au backend:', request);

    this.store.dispatch(PaymentActions.confirmPayment({ request }));
    this.loading = false;
  }

  private startPolling(): void {
    if (!this.txHash || !this.reservationId) return;

    console.log('🔥 Démarrage du polling...');
    this.store.dispatch(PaymentActions.startPolling({
      txHash: this.txHash,
      reservationId: this.reservationId
    }));
  }

  private updateStep(
    step: number,
    status: 'pending' | 'processing' | 'completed' | 'failed',
    message?: string
  ): void {
    this.store.dispatch(PaymentActions.updatePaymentStep({
      step,
      status,
      message
    }));
  }

  cancel(): void {
    if (this.reservationId) {
      this.store.dispatch(BookingActions.cancelBooking({
        id: this.reservationId,
        reason: 'Paiement annulé par l\'utilisateur'
      }));
    }

    this.close(false);
  }

  close(success: boolean): void {
    this.dialogRef.close({
      success,
      txHash: this.txHash,
      reservationId: this.reservationId
    });
  }

  /**
   * ✅ GETTER : Montant total en ETH
   * data.totalAmount est déjà en ETH depuis booking-card
   */
  get totalAmountEth(): number {
    return this.data.totalAmount;
  }

  /**
   * ✅ NOUVEAU : Conversion EUR pour affichage
   * (approximatif à 3200 EUR/ETH)
   */
  get totalAmountEur(): number {
    return this.data.totalAmount * 3200;
  }

  get canProceed(): boolean {
    switch (this.currentStep) {
      case 0:
        return true;
      case 1:
        return this.isWalletConnected;
      case 2:
        return this.hasSufficientBalance === true;
      case 3:
        return this.reservationId !== null;
      default:
        return false;
    }
  }

  get stepLabel(): string {
    const labels = [
      'Connexion wallet',
      'Vérification du solde',
      'Signature de la transaction',
      'Confirmation on-chain',
      'Réservation confirmée'
    ];
    return labels[this.currentStep] || '';
  }
}

declare global {
  interface Window {
    ethereum?: any;
  }
}
