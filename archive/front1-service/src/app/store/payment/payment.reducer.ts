// src/app/store/payment/payment.reducer.ts

import { createReducer, on } from '@ngrx/store';
import * as PaymentActions from './payment.actions';
import {
  BlockchainTransaction,
  WalletInfo,
  PaymentStatusResponse,
  TransactionStatusResponse,
  BalanceResponse,
  PaymentStep,
  createPaymentSteps
} from '../../core/models/payment.model';

/**
 * ============================
 * ÉTAT PAYMENT
 * ============================
 */
export interface PaymentState {
  // Wallet
  walletInfo: WalletInfo | null;
  walletBalance: BalanceResponse | null;

  // Vérification de solde
  hasSufficientBalance: boolean | null;
  requiredBalance: number | null;

  // Transactions
  currentTransaction: BlockchainTransaction | null;
  reservationTransactions: BlockchainTransaction[];

  // Statuts
  paymentStatus: PaymentStatusResponse | null;
  transactionStatus: TransactionStatusResponse | null;

  // Polling
  isPolling: boolean;
  pollingAttempt: number;
  pollingMaxAttempts: number;

  // Étapes de paiement (UI)
  paymentSteps: PaymentStep[];

  // État UI
  loading: boolean;
  error: string | null;
}

/**
 * ============================
 * ÉTAT INITIAL
 * ============================
 */
export const initialState: PaymentState = {
  walletInfo: null,
  walletBalance: null,
  hasSufficientBalance: null,
  requiredBalance: null,
  currentTransaction: null,
  reservationTransactions: [],
  paymentStatus: null,
  transactionStatus: null,
  isPolling: false,
  pollingAttempt: 0,
  pollingMaxAttempts: 60,
  paymentSteps: createPaymentSteps(),
  loading: false,
  error: null
};

/**
 * ============================
 * REDUCER
 * ============================
 */
export const paymentReducer = createReducer(
  initialState,

  // ========================================
  // CONNEXION WALLET
  // ========================================

  on(PaymentActions.connectWallet, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.connectWalletSuccess, (state, { walletInfo }) => ({
    ...state,
    walletInfo,
    loading: false
  })),

  on(PaymentActions.connectWalletFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  on(PaymentActions.disconnectWallet, (state) => ({
    ...state,
    walletInfo: null,
    walletBalance: null,
    hasSufficientBalance: null
  })),

  // ========================================
  // VÉRIFICATION DE SOLDE
  // ========================================

  on(PaymentActions.verifyBalance, (state) => ({
    ...state,
    loading: true,
    error: null,
    hasSufficientBalance: null
  })),

  on(PaymentActions.verifyBalanceSuccess, (state, { hasSufficientBalance, currentBalance, requiredAmount }) => ({
    ...state,
    hasSufficientBalance,
    requiredBalance: requiredAmount,
    loading: false
  })),

  on(PaymentActions.verifyBalanceFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  on(PaymentActions.loadWalletBalance, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.loadWalletBalanceSuccess, (state, { balance }) => ({
    ...state,
    walletBalance: balance,
    loading: false
  })),

  on(PaymentActions.loadWalletBalanceFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // CONFIRMATION DE PAIEMENT
  // ========================================

  on(PaymentActions.confirmPayment, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.confirmPaymentSuccess, (state, { transaction }) => ({
    ...state,
    currentTransaction: transaction,
    loading: false
  })),

  on(PaymentActions.confirmPaymentFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // STATUT DE PAIEMENT
  // ========================================

  on(PaymentActions.loadPaymentStatus, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.loadPaymentStatusSuccess, (state, { status }) => ({
    ...state,
    paymentStatus: status,
    loading: false
  })),

  on(PaymentActions.loadPaymentStatusFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  on(PaymentActions.checkTransactionStatus, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.checkTransactionStatusSuccess, (state, { status }) => ({
    ...state,
    transactionStatus: status,
    loading: false
  })),

  on(PaymentActions.checkTransactionStatusFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // POLLING
  // ========================================

  on(PaymentActions.startPolling, (state) => ({
    ...state,
    isPolling: true,
    pollingAttempt: 0,
    error: null
  })),

  on(PaymentActions.stopPolling, (state) => ({
    ...state,
    isPolling: false,
    pollingAttempt: 0
  })),

  on(PaymentActions.pollingUpdate, (state, { attempt, progress }) => ({
    ...state,
    pollingAttempt: attempt,
    pollingProgress: progress
  })),

  on(PaymentActions.pollingFailure, (state, { error }) => ({
    ...state,
    isPolling: false,
    pollingAttempt: 0,
    error,
    loading: false
  })),

  on(PaymentActions.pollingMaxAttemptsReached, (state) => ({
    ...state,
    isPolling: false,
    error: 'Délai d\'attente dépassé pour la confirmation de la transaction',
    loading: false
  })),

  on(PaymentActions.transactionConfirmed, (state, { status }) => ({
    ...state,
    transactionStatus: status,
    isPolling: false,
    pollingAttempt: 0
  })),

  on(PaymentActions.pollingTimeout, (state, { error }) => ({
    ...state,
    isPolling: false,
    pollingAttempt: 0,
    error
  })),

  // ========================================
  // HISTORIQUE DES TRANSACTIONS
  // ========================================

  on(PaymentActions.loadReservationTransactions, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.loadReservationTransactionsSuccess, (state, { transactions }) => ({
    ...state,
    reservationTransactions: transactions,
    loading: false
  })),

  on(PaymentActions.loadReservationTransactionsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // LIBÉRATION ESCROW
  // ========================================

  on(PaymentActions.releaseEscrow, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.releaseEscrowSuccess, (state, { transaction }) => ({
    ...state,
    currentTransaction: transaction,
    loading: false
  })),

  on(PaymentActions.releaseEscrowFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // REMBOURSEMENT
  // ========================================

  on(PaymentActions.processRefund, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(PaymentActions.processRefundSuccess, (state, { transaction }) => ({
    ...state,
    currentTransaction: transaction,
    loading: false
  })),

  on(PaymentActions.processRefundFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // ÉTAPES DE PAIEMENT (UI)
  // ========================================

  on(PaymentActions.initPaymentSteps, (state) => ({
    ...state,
    paymentSteps: createPaymentSteps()
  })),

  on(PaymentActions.updatePaymentStep, (state, { step, status, message }) => ({
    ...state,
    paymentSteps: state.paymentSteps.map(s =>
      s.step === step ? { ...s, status, message } : s
    )
  })),

  on(PaymentActions.resetPaymentSteps, (state) => ({
    ...state,
    paymentSteps: createPaymentSteps()
  })),

  // ========================================
  // TRANSACTION COURANTE
  // ========================================

  on(PaymentActions.setCurrentTransaction, (state, { transaction }) => ({
    ...state,
    currentTransaction: transaction
  })),

  on(PaymentActions.clearCurrentTransaction, (state) => ({
    ...state,
    currentTransaction: null
  })),

  // ========================================
  // RESET
  // ========================================

  on(PaymentActions.resetPaymentState, () => initialState),

  on(PaymentActions.clearPaymentError, (state) => ({
    ...state,
    error: null
  }))
);
