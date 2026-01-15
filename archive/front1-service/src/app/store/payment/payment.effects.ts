// src/app/store/payment/payment.effects.ts
// ✅ VERSION CORRIGÉE - TOUTES LES ERREURS RÉSOLUES

import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of, timer, EMPTY } from 'rxjs';
import { map, catchError, exhaustMap, tap, switchMap, takeUntil, retry } from 'rxjs/operators';
import { PaymentService } from '../../core/services/payment.service';
import { Web3Service } from '../../core/services/web3.service';
import * as PaymentActions from './payment.actions';

/**
 * ============================
 * PAYMENT EFFECTS
 * Gère tous les side effects pour les paiements
 * ============================
 */
@Injectable()
export class PaymentEffects {

  private actions$ = inject(Actions);
  private paymentService = inject(PaymentService);
  private web3Service = inject(Web3Service);

  // ========================================
  // CONNEXION WALLET
  // ========================================

  connectWallet$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.connectWallet),
      tap(() => console.log('🔥 Effect: connectWallet')),
      exhaustMap(() =>
        this.web3Service.connectWallet().pipe(
          map(address => {
            console.log('✅ Wallet connected:', address);

            const walletInfo = {
              address,
              balance: 0,
              network: 'sepolia',
              isConnected: true
            };

            return PaymentActions.connectWalletSuccess({ walletInfo });
          }),
          catchError(error => {
            console.error('❌ Error connecting wallet:', error);
            return of(PaymentActions.connectWalletFailure({
              error: error.message || 'Erreur lors de la connexion au wallet'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // VÉRIFICATION DE SOLDE
  // ========================================

  verifyBalance$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.verifyBalance),
      tap(action => console.log('🔥 Effect: verifyBalance', action.request)),
      exhaustMap(({ request }) =>
        this.paymentService.verifyBalance(request).pipe(
          map(response => {
            console.log('✅ Balance verified:', response);

            // ✅ Calculer si le solde est suffisant
            const hasSufficientBalance = response.balanceEth >= request.requiredAmountEth;

            return PaymentActions.verifyBalanceSuccess({
              hasSufficientBalance: hasSufficientBalance,
              currentBalance: response.balanceEth,
              requiredAmount: request.requiredAmountEth
            });
          }),
          catchError(error => {
            console.error('❌ Error verifying balance:', error);
            return of(PaymentActions.verifyBalanceFailure({
              error: error.message || 'Erreur lors de la vérification du solde'
            }));
          })
        )
      )
    )
  );

  loadWalletBalance$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.loadWalletBalance),
      switchMap(({ walletAddress, requiredAmountEth }) =>
        this.paymentService.getWalletBalance(walletAddress).pipe(
          map(({ balanceEth }) =>
            PaymentActions.verifyBalanceSuccess({
              hasSufficientBalance: balanceEth >= requiredAmountEth,
              currentBalance: balanceEth,
              requiredAmount: requiredAmountEth
            })
          ),
          catchError(err =>
            of(PaymentActions.verifyBalanceFailure({
              error: 'Impossible de récupérer le solde'
            }))
          )
        )
      )
    )
  );


  // ========================================
  // CONFIRMATION DE PAIEMENT
  // ========================================

  confirmPayment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.confirmPayment),
      tap(action => console.log('🔥 Effect: confirmPayment', action.request)),
      exhaustMap(({ request }) =>
        this.paymentService.confirmPayment(request).pipe(
          map(transaction => {
            console.log('✅ Payment confirmed:', transaction);
            return PaymentActions.confirmPaymentSuccess({ transaction });
          }),
          catchError(error => {
            console.error('❌ Error confirming payment:', error);
            return of(PaymentActions.confirmPaymentFailure({
              error: error.message || 'Erreur lors de la confirmation du paiement'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // STATUT DE PAIEMENT
  // ========================================

  loadPaymentStatus$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.loadPaymentStatus),
      tap(action => console.log('🔥 Effect: loadPaymentStatus', action.reservationId)),
      exhaustMap(({ reservationId }) =>
        this.paymentService.getPaymentStatus(reservationId).pipe(
          map(status => {
            console.log('✅ Payment status loaded:', status);
            return PaymentActions.loadPaymentStatusSuccess({ status });
          }),
          catchError(error => {
            console.error('❌ Error loading payment status:', error);
            return of(PaymentActions.loadPaymentStatusFailure({
              error: error.message || 'Erreur lors du chargement du statut'
            }));
          })
        )
      )
    )
  );

  checkTransactionStatus$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.checkTransactionStatus),
      tap(action => console.log('🔥 Effect: checkTransactionStatus', action.txHash)),
      exhaustMap(({ txHash }) =>
        this.paymentService.checkTransactionStatus(txHash).pipe( // ✅ checkTransactionStatus
          map(status => {
            console.log('✅ Transaction status:', status.status);
            return PaymentActions.checkTransactionStatusSuccess({ status });
          }),
          catchError(error => {
            console.error('❌ Error checking transaction status:', error);
            return of(PaymentActions.checkTransactionStatusFailure({
              error: error.message || 'Erreur lors de la vérification du statut'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // POLLING DE CONFIRMATION
  // ========================================

  startPolling$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.startPolling),
      tap(action => console.log('🔥 Effect: startPolling', action)),
      switchMap(({ txHash, reservationId }) => {
        let attempts = 0;
        const maxAttempts = 60; // 5 minutes max (60 * 5s)

        return timer(0, 5000).pipe( // Polling toutes les 5 secondes
          takeUntil(this.actions$.pipe(ofType(PaymentActions.stopPolling))),
          switchMap(() => {
            attempts++;
            console.log(`🔍 Polling attempt ${attempts}/${maxAttempts}`);

            if (attempts > maxAttempts) {
              console.warn('⏱️ Max polling attempts reached');
              return of(PaymentActions.pollingMaxAttemptsReached());
            }

            return this.paymentService.checkTransactionStatus(txHash).pipe( // ✅ checkTransactionStatus
              map(status => {
                const progress = Math.min((attempts / maxAttempts) * 100, 100);

                if (status.status === 'CONFIRMED') {
                  console.log('✅ Transaction confirmed on-chain!');
                  return PaymentActions.transactionConfirmed({ status });
                } else if (status.status === 'FAILED') {
                  console.error('❌ Transaction failed on-chain');
                  return PaymentActions.pollingFailure({
                    error: 'Transaction failed on blockchain'
                  });
                } else {
                  return PaymentActions.pollingUpdate({
                    attempt: attempts,
                    progress
                  });
                }
              }),
              retry(2), // Retry 2 fois en cas d'erreur réseau
              catchError(error => {
                console.error('❌ Polling error:', error);
                return of(PaymentActions.pollingFailure({
                  error: error.message || 'Erreur lors du polling'
                }));
              })
            );
          })
        );
      })
    )
  );

  // ========================================
  // TRANSACTIONS
  // ========================================

  loadReservationTransactions$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.loadReservationTransactions),
      tap(action => console.log('🔥 Effect: loadReservationTransactions', action.reservationId)),
      exhaustMap(({ reservationId }) =>
        this.paymentService.getReservationTransactions(reservationId).pipe(
          map(transactions => {
            console.log('✅ Transactions loaded:', transactions.length);
            return PaymentActions.loadReservationTransactionsSuccess({ transactions });
          }),
          catchError(error => {
            console.error('❌ Error loading transactions:', error);
            return of(PaymentActions.loadReservationTransactionsFailure({
              error: error.message || 'Erreur lors du chargement des transactions'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // ESCROW
  // ========================================

  releaseEscrow$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.releaseEscrow),
      tap(action => console.log('🔥 Effect: releaseEscrow', action.reservationId)),
      exhaustMap(({ reservationId }) =>
        this.paymentService.releaseEscrow(reservationId).pipe(
          map(transaction => {
            console.log('✅ Escrow released:', transaction);
            return PaymentActions.releaseEscrowSuccess({ transaction });
          }),
          catchError(error => {
            console.error('❌ Error releasing escrow:', error);
            return of(PaymentActions.releaseEscrowFailure({
              error: error.message || 'Erreur lors de la libération de l\'escrow'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // REMBOURSEMENT
  // ========================================

  processRefund$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PaymentActions.processRefund),
      tap(action => console.log('🔥 Effect: processRefund', action)),
      exhaustMap(({ reservationId, reason }) =>
        this.paymentService.processRefund(reservationId, reason || 'Remboursement demandé').pipe( // ✅ 2 arguments seulement
          map(transaction => {
            console.log('✅ Refund processed:', transaction);
            return PaymentActions.processRefundSuccess({ transaction });
          }),
          catchError(error => {
            console.error('❌ Error processing refund:', error);
            return of(PaymentActions.processRefundFailure({
              error: error.message || 'Erreur lors du remboursement'
            }));
          })
        )
      )
    )
  );
}
