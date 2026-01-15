// src/app/core/models/payment.model.ts

/**
 * ============================
 * ENUM PaymentStatus
 * Correspond EXACTEMENT au backend PaymentStatus
 * ============================
 */
export enum PaymentStatus {
  PENDING = 'PENDING',           // En attente
  PROCESSING = 'PROCESSING',     // En cours de traitement
  CONFIRMED = 'CONFIRMED',       // Confirmé on-chain
  FAILED = 'FAILED'             // Échoué
}

/**
 * ============================
 * ENUM PaymentType
 * Correspond EXACTEMENT au backend PaymentType
 * ============================
 */
export enum PaymentType {
  BOOKING_PAYMENT = 'BOOKING_PAYMENT',   // Paiement de réservation
  ESCROW_RELEASE = 'ESCROW_RELEASE',     // Libération escrow au propriétaire
  PLATFORM_FEE = 'PLATFORM_FEE',         // Frais de plateforme
  REFUND = 'REFUND'                      // Remboursement
}

/**
 * ============================
 * Interface BlockchainTransaction
 * Correspond au PaymentResponse backend
 * ============================
 */
export interface BlockchainTransaction {
  transactionId: number;
  transactionHash: string;           // Hash Ethereum (0x...)
  status: PaymentStatus;
  amountEth: number;
  gasFeeEth?: number;
  blockNumber?: number;
  createdAt: string;
  message?: string;
  explorerUrl?: string;              // Lien Etherscan
}

/**
 * ============================
 * DTO PaymentRequest
 * Pour créer un paiement initial
 * ⚠️ NON UTILISÉ dans le flow MetaMask
 * (On utilise SignedTransactionRequest à la place)
 * ============================
 */
export interface PaymentRequest {
  reservationId: number;
  tenantWalletAddress: string;       // Adresse du locataire
  hostWalletAddress: string;         // Adresse du propriétaire
  totalAmountEth: number;
}

/**
 * ============================
 * DTO SignedTransactionRequest
 * ✅ UTILISÉ pour confirmer un paiement MetaMask
 * Correspond EXACTEMENT au SignedTransactionRequest backend
 * ============================
 */
export interface SignedTransactionRequest {
  reservationId: number;
  transactionHash: string;           // Hash retourné par MetaMask
  fromAddress: string;               // Adresse du locataire (user wallet)
  amountEth: number;
  tenantId: number;                  // ID du locataire
}

/**
 * ============================
 * DTO BalanceVerificationRequest
 * Pour vérifier le solde avant paiement
 * ============================
 */
export interface BalanceVerificationRequest {
  walletAddress: string;
  requiredAmountEth: number;
}

/**
 * ============================
 * Interface BalanceResponse
 * Réponse de vérification de solde
 * ============================
 */
export interface BalanceResponse {
  walletAddress: string;
  balanceEth: number;
  balanceUsd?: number;               // Optionnel: conversion USD
  message: string;
}

/**
 * ============================
 * Interface PaymentStatus (détaillé)
 * Réponse complète du statut d'un paiement
 * ============================
 */
export interface PaymentStatusResponse {
  reservationId: number;
  hasConfirmedPayment: boolean;
  totalPaid: number;
  transactionCount: number;
  transactions: BlockchainTransaction[];
  timestamp: string;
}

/**
 * ============================
 * Interface TransactionStatusResponse
 * Statut on-chain d'une transaction
 * ============================
 */
export interface TransactionStatusResponse {
  transactionHash: string;
  status: 'CONFIRMED' | 'PENDING' | 'FAILED';
  gasFeeEth: number;
  message: string;
  timestamp: string;
}

/**
 * ============================
 * Interface WalletInfo
 * Informations du wallet MetaMask
 * ============================
 */
export interface WalletInfo {
  address: string;
  balance: number;
  network: string;                   // 'sepolia', 'mainnet', etc.
  isConnected: boolean;
}

/**
 * ============================
 * Interface PaymentStep
 * Pour afficher le progress dans l'UI
 * ============================
 */
export interface PaymentStep {
  step: number;
  label: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  message?: string;
}

/**
 * ============================
 * Helpers / Utility Functions
 * ============================
 */

/**
 * Convertir Wei en ETH
 */
export function weiToEth(wei: string | number): number {
  const weiValue = typeof wei === 'string' ? parseFloat(wei) : wei;
  return weiValue / 1e18;
}

/**
 * Convertir ETH en Wei
 */
export function ethToWei(eth: number): string {
  return (eth * 1e18).toString();
}

/**
 * Formater une adresse Ethereum (raccourcie)
 */
export function formatAddress(address: string): string {
  if (!address || address.length < 10) return address;
  return `${address.substring(0, 6)}...${address.substring(address.length - 4)}`;
}

/**
 * Obtenir le lien Etherscan
 */
export function getEtherscanUrl(txHash: string, network: string = 'sepolia'): string {
  const baseUrls: Record<string, string> = {
    mainnet: 'https://etherscan.io',
    sepolia: 'https://sepolia.etherscan.io',
    goerli: 'https://goerli.etherscan.io'
  };

  const baseUrl = baseUrls[network] || baseUrls["sepolia"];
  return `${baseUrl}/tx/${txHash}`;
}

/**
 * Vérifier si une adresse Ethereum est valide
 */
export function isValidEthereumAddress(address: string): boolean {
  return /^0x[a-fA-F0-9]{40}$/.test(address);
}

/**
 * Vérifier si un hash de transaction est valide
 */
export function isValidTransactionHash(hash: string): boolean {
  return /^0x[a-fA-F0-9]{64}$/.test(hash);
}

/**
 * Obtenir le label du statut de paiement
 */
export function getPaymentStatusLabel(status: PaymentStatus): string {
  const labels: Record<PaymentStatus, string> = {
    [PaymentStatus.PENDING]: 'En attente',
    [PaymentStatus.PROCESSING]: 'En cours',
    [PaymentStatus.CONFIRMED]: 'Confirmé',
    [PaymentStatus.FAILED]: 'Échoué'
  };
  return labels[status];
}

/**
 * Obtenir la couleur du statut de paiement
 */
export function getPaymentStatusColor(status: PaymentStatus): string {
  const colors: Record<PaymentStatus, string> = {
    [PaymentStatus.PENDING]: '#FFA500',      // Orange
    [PaymentStatus.PROCESSING]: '#0066FF',   // Bleu
    [PaymentStatus.CONFIRMED]: '#00A699',    // Vert
    [PaymentStatus.FAILED]: '#FF385C'        // Rouge
  };
  return colors[status];
}

/**
 * Créer les étapes de paiement pour l'UI
 */
export function createPaymentSteps(): PaymentStep[] {
  return [
    {
      step: 1,
      label: 'Connexion wallet',
      status: 'pending'
    },
    {
      step: 2,
      label: 'Vérification du solde',
      status: 'pending'
    },
    {
      step: 3,
      label: 'Signature de la transaction',
      status: 'pending'
    },
    {
      step: 4,
      label: 'Confirmation on-chain',
      status: 'pending'
    },
    {
      step: 5,
      label: 'Réservation confirmée',
      status: 'pending'
    }
  ];
}
