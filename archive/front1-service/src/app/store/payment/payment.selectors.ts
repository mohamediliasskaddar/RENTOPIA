// src/app/store/payment/payment.selectors.ts

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { PaymentState } from './payment.reducer';
import { PaymentStatus } from '../../core/models/payment.model';

/**
 * ============================
 * PAYMENT SELECTORS
 * Sélecteurs pour accéder à l'état des paiements
 * ============================
 */

// Sélecteur de feature
export const selectPaymentState = createFeatureSelector<PaymentState>('payment');

// ========================================
// WALLET
// ========================================

/**
 * Informations du wallet
 */
export const selectWalletInfo = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.walletInfo
);

/**
 * Solde du wallet
 */
export const selectWalletBalance = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.walletBalance
);

/**
 * Adresse du wallet
 */
export const selectWalletAddress = createSelector(
  selectWalletInfo,
  (walletInfo) => walletInfo?.address || null
);

/**
 * Vérifier si le wallet est connecté
 */
export const selectIsWalletConnected = createSelector(
  selectWalletInfo,
  (walletInfo) => walletInfo?.isConnected || false
);

/**
 * Solde en ETH
 */
export const selectBalanceEth = createSelector(
  selectWalletBalance,
  (balance) => balance?.balanceEth || 0
);

// ========================================
// VÉRIFICATION DE SOLDE
// ========================================

/**
 * A suffisamment de solde
 */
export const selectHasSufficientBalance = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.hasSufficientBalance
);

/**
 * Montant requis
 */
export const selectRequiredBalance = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.requiredBalance
);

// ========================================
// TRANSACTIONS
// ========================================

/**
 * Transaction courante
 */
export const selectCurrentTransaction = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.currentTransaction
);

/**
 * Transactions d'une réservation
 */
export const selectReservationTransactions = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.reservationTransactions
);

/**
 * Hash de la transaction courante
 */
export const selectCurrentTransactionHash = createSelector(
  selectCurrentTransaction,
  (transaction) => transaction?.transactionHash || null
);

/**
 * Statut de la transaction courante
 */
export const selectCurrentTransactionStatus = createSelector(
  selectCurrentTransaction,
  (transaction) => transaction?.status || null
);

// ========================================
// STATUTS
// ========================================

/**
 * Statut du paiement
 */
export const selectPaymentStatus = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.paymentStatus
);

/**
 * Statut de la transaction on-chain
 */
export const selectTransactionStatus = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.transactionStatus
);

/**
 * Vérifier si le paiement est confirmé
 */
export const selectIsPaymentConfirmed = createSelector(
  selectPaymentStatus,
  (status) => status?.hasConfirmedPayment || false
);

/**
 * Montant total payé
 */
export const selectTotalPaid = createSelector(
  selectPaymentStatus,
  (status) => status?.totalPaid || 0
);

// ========================================
// POLLING
// ========================================

/**
 * Polling en cours
 */
export const selectIsPolling = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.isPolling
);

/**
 * Tentative de polling courante
 */
export const selectPollingAttempt = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.pollingAttempt
);

/**
 * Nombre maximum de tentatives
 */
export const selectPollingMaxAttempts = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.pollingMaxAttempts
);

/**
 * Progression du polling (pourcentage)
 */
export const selectPollingProgress = createSelector(
  selectPollingAttempt,
  selectPollingMaxAttempts,
  (attempt, maxAttempts) => {
    if (maxAttempts === 0) return 0;
    return Math.round((attempt / maxAttempts) * 100);
  }
);

// ========================================
// ÉTAPES DE PAIEMENT (UI)
// ========================================

/**
 * Étapes de paiement
 */
export const selectPaymentSteps = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.paymentSteps
);

/**
 * Étape courante (la première non completed)
 */
export const selectCurrentPaymentStep = createSelector(
  selectPaymentSteps,
  (steps) => {
    const currentStep = steps.find(s => s.status !== 'completed');
    return currentStep?.step || steps.length;
  }
);

/**
 * Progression des étapes (pourcentage)
 */
export const selectPaymentStepsProgress = createSelector(
  selectPaymentSteps,
  (steps) => {
    const completed = steps.filter(s => s.status === 'completed').length;
    return Math.round((completed / steps.length) * 100);
  }
);

/**
 * Vérifier si toutes les étapes sont complétées
 */
export const selectAreAllStepsCompleted = createSelector(
  selectPaymentSteps,
  (steps) => steps.every(s => s.status === 'completed')
);

/**
 * Vérifier si une étape a échoué
 */
export const selectHasFailedStep = createSelector(
  selectPaymentSteps,
  (steps) => steps.some(s => s.status === 'failed')
);

// ========================================
// ÉTAT UI
// ========================================

/**
 * Chargement en cours
 */
export const selectPaymentLoading = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.loading
);

/**
 * Erreur
 */
export const selectPaymentError = createSelector(
  selectPaymentState,
  (state: PaymentState) => state.error
);

/**
 * A une erreur
 */
export const selectHasPaymentError = createSelector(
  selectPaymentError,
  (error) => error !== null
);

// ========================================
// SÉLECTEURS DÉRIVÉS (COMPUTED)
// ========================================

/**
 * Vérifier si le processus de paiement est en cours
 */
export const selectIsPaymentInProgress = createSelector(
  selectPaymentLoading,
  selectIsPolling,
  (loading, polling) => loading || polling
);

/**
 * Nombre total de transactions
 */
export const selectTransactionCount = createSelector(
  selectReservationTransactions,
  (transactions) => transactions.length
);

/**
 * Transactions confirmées
 */
export const selectConfirmedTransactions = createSelector(
  selectReservationTransactions,
  (transactions) => transactions.filter(t => t.status === PaymentStatus.CONFIRMED)
);

/**
 * Transactions en attente
 */
export const selectPendingTransactions = createSelector(
  selectReservationTransactions,
  (transactions) => transactions.filter(t => t.status === PaymentStatus.PENDING)
);

/**
 * Transactions échouées
 */
export const selectFailedTransactions = createSelector(
  selectReservationTransactions,
  (transactions) => transactions.filter(t => t.status === PaymentStatus.FAILED)
);

/**
 * Lien Etherscan de la transaction courante
 */
export const selectCurrentTransactionExplorerUrl = createSelector(
  selectCurrentTransaction,
  (transaction) => transaction?.explorerUrl || null
);

/**
 * Frais de gas de la transaction courante
 */
export const selectCurrentTransactionGasFee = createSelector(
  selectCurrentTransaction,
  (transaction) => transaction?.gasFeeEth || 0
);

/**
 * Montant total de la transaction courante
 */
export const selectCurrentTransactionAmount = createSelector(
  selectCurrentTransaction,
  (transaction) => transaction?.amountEth || 0
);

/**
 * Vérifier si la transaction est confirmée on-chain
 */
export const selectIsTransactionConfirmed = createSelector(
  selectTransactionStatus,
  (status) => status?.status === 'CONFIRMED'
);

/**
 * Message de statut de la transaction
 */
export const selectTransactionStatusMessage = createSelector(
  selectTransactionStatus,
  (status) => status?.message || null
);

/**
 * Informations complètes du wallet (adresse + solde)
 */
export const selectWalletDetails = createSelector(
  selectWalletInfo,
  selectWalletBalance,
  (info, balance) => ({
    address: info?.address || null,
    isConnected: info?.isConnected || false,
    balanceEth: balance?.balanceEth || 0,
    balanceUsd: balance?.balanceUsd || 0,
    network: info?.network || null
  })
);

/**
 * État complet du processus de paiement
 */
export const selectPaymentProcess = createSelector(
  selectPaymentSteps,
  selectIsPolling,
  selectCurrentTransaction,
  selectPaymentError,
  (steps, isPolling, transaction, error) => ({
    steps,
    isPolling,
    currentTransaction: transaction,
    error,
    currentStep: steps.find(s => s.status !== 'completed')?.step || steps.length,
    progress: Math.round((steps.filter(s => s.status === 'completed').length / steps.length) * 100),
    isCompleted: steps.every(s => s.status === 'completed'),
    hasFailed: steps.some(s => s.status === 'failed')
  })
);

/**
 * Récapitulatif des transactions
 */
export const selectTransactionsSummary = createSelector(
  selectReservationTransactions,
  (transactions) => ({
    total: transactions.length,
    confirmed: transactions.filter(t => t.status === PaymentStatus.CONFIRMED).length,
    pending: transactions.filter(t => t.status === PaymentStatus.PENDING).length,
    failed: transactions.filter(t => t.status === PaymentStatus.FAILED).length,
    totalAmount: transactions
      .filter(t => t.status === PaymentStatus.CONFIRMED)
      .reduce((sum, t) => sum + t.amountEth, 0),
    totalGasFees: transactions
      .filter(t => t.status === PaymentStatus.CONFIRMED)
      .reduce((sum, t) => sum + (t.gasFeeEth || 0), 0)
  })
);
