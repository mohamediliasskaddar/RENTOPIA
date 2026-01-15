// src/app/store/payment/payment.actions.ts

import { createAction, props } from '@ngrx/store';
import {
  BlockchainTransaction,
  SignedTransactionRequest,
  BalanceVerificationRequest,
  BalanceResponse,
  PaymentStatusResponse,
  TransactionStatusResponse,
  WalletInfo,
  PaymentStep
} from '../../core/models/payment.model';

/**
 * ============================
 * ACTIONS PAYMENT
 * Toutes les actions pour gérer les paiements
 * ============================
 */

// ========================================
// CONNEXION WALLET
// ========================================

/**
 * Connecter le wallet MetaMask
 */
export const connectWallet = createAction(
  '[Payment] Connect Wallet'
);

export const connectWalletSuccess = createAction(
  '[Payment] Connect Wallet Success',
  props<{ walletInfo: WalletInfo }>()
);

export const connectWalletFailure = createAction(
  '[Payment] Connect Wallet Failure',
  props<{ error: string }>()
);

/**
 * Déconnecter le wallet
 */
export const disconnectWallet = createAction(
  '[Payment] Disconnect Wallet'
);

// ========================================
// VÉRIFICATION DE SOLDE
// ========================================

/**
 * Vérifier le solde avant paiement
 */
export const verifyBalance = createAction(
  '[Payment] Verify Balance',
  props<{ request: BalanceVerificationRequest }>()
);

export const verifyBalanceSuccess = createAction(
  '[Payment] Verify Balance Success',
  props<{ hasSufficientBalance: boolean; currentBalance: number; requiredAmount: number }>()
);

export const verifyBalanceFailure = createAction(
  '[Payment] Verify Balance Failure',
  props<{ error: string }>()
);

/**
 * Charger le solde d'un wallet
 */
export const loadWalletBalance = createAction(
  '[Payment] Load Wallet Balance',
  props<{ walletAddress: string; requiredAmountEth: number }>()
);

export const loadWalletBalanceSuccess = createAction(
  '[Payment] Load Wallet Balance Success',
  props<{ balance: BalanceResponse }>()
);

export const loadWalletBalanceFailure = createAction(
  '[Payment] Load Wallet Balance Failure',
  props<{ error: string }>()
);

// ========================================
// CONFIRMATION DE PAIEMENT (APRÈS METAMASK)
// ========================================

/**
 * Confirmer un paiement après signature MetaMask
 * Flow:
 * 1. User signe transaction avec MetaMask
 * 2. Frontend récupère txHash
 * 3. Dispatch confirmPayment
 * 4. Backend enregistre transaction
 * 5. Backend confirme booking
 */
export const confirmPayment = createAction(
  '[Payment] Confirm Payment',
  props<{ request: SignedTransactionRequest }>()
);

export const confirmPaymentSuccess = createAction(
  '[Payment] Confirm Payment Success',
  props<{ transaction: BlockchainTransaction }>()
);

export const confirmPaymentFailure = createAction(
  '[Payment] Confirm Payment Failure',
  props<{ error: string }>()
);

// ========================================
// STATUT DE PAIEMENT
// ========================================

/**
 * Charger le statut d'un paiement
 */
export const loadPaymentStatus = createAction(
  '[Payment] Load Payment Status',
  props<{ reservationId: number }>()
);

export const loadPaymentStatusSuccess = createAction(
  '[Payment] Load Payment Status Success',
  props<{ status: PaymentStatusResponse }>()
);

export const loadPaymentStatusFailure = createAction(
  '[Payment] Load Payment Status Failure',
  props<{ error: string }>()
);

/**
 * Vérifier le statut on-chain d'une transaction
 */
export const checkTransactionStatus = createAction(
  '[Payment] Check Transaction Status',
  props<{ txHash: string }>()
);

export const checkTransactionStatusSuccess = createAction(
  '[Payment] Check Transaction Status Success',
  props<{ status: TransactionStatusResponse }>()
);

export const checkTransactionStatusFailure = createAction(
  '[Payment] Check Transaction Status Failure',
  props<{ error: string }>()
);

// ========================================
// POLLING DE CONFIRMATION
// ========================================

/**
 * Démarrer le polling pour attendre confirmation on-chain
 */
export const startPolling = createAction(
  '[Payment] Start Polling',
  props<{ txHash: string; reservationId: number }>()
);

/**
 * Arrêter le polling
 */
export const stopPolling = createAction(
  '[Payment] Stop Polling'
);

/**
 * Mise à jour du polling
 */
export const pollingUpdate = createAction(
  '[Payment] Polling Update',
  props<{ attempt: number; progress: number }>()
);

/**
 * Polling a échoué
 */
export const pollingFailure = createAction(
  '[Payment] Polling Failure',
  props<{ error: string }>()
);

/**
 * Nombre maximum de tentatives de polling atteint
 */
export const pollingMaxAttemptsReached = createAction(
  '[Payment] Polling Max Attempts Reached'
);

/**
 * Transaction confirmée on-chain (via polling)
 */
export const transactionConfirmed = createAction(
  '[Payment] Transaction Confirmed',
  props<{ status: TransactionStatusResponse }>()
);

/**
 * Timeout du polling
 */
export const pollingTimeout = createAction(
  '[Payment] Polling Timeout',
  props<{ error: string }>()
);

// ========================================
// HISTORIQUE DES TRANSACTIONS
// ========================================

/**
 * Charger les transactions d'une réservation
 */
export const loadReservationTransactions = createAction(
  '[Payment] Load Reservation Transactions',
  props<{ reservationId: number }>()
);

export const loadReservationTransactionsSuccess = createAction(
  '[Payment] Load Reservation Transactions Success',
  props<{ transactions: BlockchainTransaction[] }>()
);

export const loadReservationTransactionsFailure = createAction(
  '[Payment] Load Reservation Transactions Failure',
  props<{ error: string }>()
);

// ========================================
// LIBÉRATION ESCROW
// ========================================

/**
 * Libérer l'escrow au propriétaire
 */
export const releaseEscrow = createAction(
  '[Payment] Release Escrow',
  props<{ reservationId: number }>()
);

export const releaseEscrowSuccess = createAction(
  '[Payment] Release Escrow Success',
  props<{ transaction: BlockchainTransaction }>()
);

export const releaseEscrowFailure = createAction(
  '[Payment] Release Escrow Failure',
  props<{ error: string }>()
);

// ========================================
// REMBOURSEMENT
// ========================================

/**
 * Initier un remboursement
 */
export const processRefund = createAction(
  '[Payment] Process Refund',
  props<{ reservationId: number; amount?: number; reason?: string }>()
);

export const processRefundSuccess = createAction(
  '[Payment] Process Refund Success',
  props<{ transaction: BlockchainTransaction }>()
);

export const processRefundFailure = createAction(
  '[Payment] Process Refund Failure',
  props<{ error: string }>()
);

// ========================================
// ÉTAPES DE PAIEMENT (UI)
// ========================================

/**
 * Initialiser les étapes de paiement
 */
export const initPaymentSteps = createAction(
  '[Payment] Init Payment Steps'
);

/**
 * Mettre à jour une étape de paiement
 */
export const updatePaymentStep = createAction(
  '[Payment] Update Payment Step',
  props<{ step: number; status: 'pending' | 'processing' | 'completed' | 'failed'; message?: string }>()
);

/**
 * Réinitialiser les étapes de paiement
 */
export const resetPaymentSteps = createAction(
  '[Payment] Reset Payment Steps'
);

// ========================================
// TRANSACTION COURANTE
// ========================================

/**
 * Définir la transaction courante
 */
export const setCurrentTransaction = createAction(
  '[Payment] Set Current Transaction',
  props<{ transaction: BlockchainTransaction }>()
);

/**
 * Effacer la transaction courante
 */
export const clearCurrentTransaction = createAction(
  '[Payment] Clear Current Transaction'
);

// ========================================
// RESET / CLEAR
// ========================================

/**
 * Réinitialiser l'état payment
 */
export const resetPaymentState = createAction(
  '[Payment] Reset State'
);

/**
 * Effacer les erreurs
 */
export const clearPaymentError = createAction(
  '[Payment] Clear Error'
);
