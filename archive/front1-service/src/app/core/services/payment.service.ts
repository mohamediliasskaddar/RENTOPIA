// src/app/core/services/payment.service.ts

import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  WalletInfo,
  BalanceVerificationRequest,
  BalanceResponse,
  SignedTransactionRequest,
  BlockchainTransaction,
  PaymentStatusResponse,
  TransactionStatusResponse
} from '../models/payment.model';

/**
 * ============================
 * PAYMENT SERVICE
 * Gère toutes les interactions avec l'API Payment
 * ============================
 */
@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private apiService = inject(ApiService);

  constructor() {
    console.log('✅ PaymentService initialized');
  }

  /**
   * ============================
   * CONNEXION WALLET
   * ============================
   */
  connectWallet(walletAddress: string, signature: string): Observable<WalletInfo> {
    console.log('📤 Connecting wallet:', walletAddress);
    return this.apiService.post<WalletInfo>('/connect-wallet', {
      walletAddress,
      signature
    });
  }

  /**
   * ============================
   * VÉRIFIER LE SOLDE
   * ============================
   */
  verifyBalance(request: BalanceVerificationRequest): Observable<BalanceResponse> {
    console.log('📤 Verifying balance:', request);
    return this.apiService.post<BalanceResponse>(
      '/blockchain/balance/verify',
      request
    );
  }

  /**
   * ============================
   * RÉCUPÉRER LE SOLDE DU WALLET
   * ============================
   */
  getWalletBalance(walletAddress: string): Observable<BalanceResponse> {
    console.log('📤 Getting wallet balance:', walletAddress);
    return this.apiService.get<BalanceResponse>(
      `/payments/balance/${walletAddress}`
    );
  }

  /**
   * ============================
   * CONFIRMER LE PAIEMENT
   * ============================
   */
  confirmPayment(request: SignedTransactionRequest): Observable<BlockchainTransaction> {
    console.log('📤 Confirming payment:', request);
    return this.apiService.post<BlockchainTransaction>(
      '/confirm-payment',
      request
    );
  }

  /**
   * ============================
   * RÉCUPÉRER LE STATUT DU PAIEMENT
   * ============================
   */
  getPaymentStatus(reservationId: number): Observable<PaymentStatusResponse> {
    console.log('📤 Getting payment status for reservation:', reservationId);
    return this.apiService.get<PaymentStatusResponse>(
      `/reservation/${reservationId}/status`
    );
  }

  /**
   * ============================
   * VÉRIFIER LE STATUT DE LA TRANSACTION
   * ============================
   */
  checkTransactionStatus(txHash: string): Observable<TransactionStatusResponse> {
    console.log('📤 Checking transaction status:', txHash);
    return this.apiService.get<TransactionStatusResponse>(
      `/transaction/${txHash}/status`
    );
  }

  /**
   * ============================
   * RÉCUPÉRER LES TRANSACTIONS D'UNE RÉSERVATION
   * ============================
   */
  getReservationTransactions(reservationId: number): Observable<BlockchainTransaction[]> {
    console.log('📤 Getting transactions for reservation:', reservationId);
    return this.apiService.get<BlockchainTransaction[]>(
      `/reservation/${reservationId}/transactions`
    );
  }

  /**
   * ============================
   * LIBÉRER L'ESCROW
   * ============================
   */
  releaseEscrow(reservationId: number): Observable<BlockchainTransaction> {
    console.log('📤 Releasing escrow for reservation:', reservationId);
    return this.apiService.post<BlockchainTransaction>(
      `/reservation/${reservationId}/release-escrow`,
      {}
    );
  }

  /**
   * ============================
   * TRAITER UN REMBOURSEMENT
   * ============================
   */
  processRefund(reservationId: number, reason: string): Observable<BlockchainTransaction> {
    console.log('📤 Processing refund:', { reservationId, reason });
    return this.apiService.post<BlockchainTransaction>(
      '/refund',
      { reservationId, reason }
    );
  }
}
