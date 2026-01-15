// src/app/core/models/auth.model.ts

import { UserResponseDTO } from './user.model';

/**
 * ============================
 * Interface RegisterDTO
 * Correspond EXACTEMENT au RegisterDTO backend
 * Utilisée pour l'inscription (MetaMask + formulaire)
 * ============================
 */
export interface RegisterDTO {
  nom: string;          // Obligatoire, min: 2, max: 100
  prenom: string;       // Obligatoire, min: 2, max: 100
  email: string;        // Obligatoire, doit être un email valide
  password: string;     // Obligatoire, min: 8, max: 100
  walletAdresse: string; // Obligatoire (récupéré de MetaMask)
  tel?: string;         // Optionnel, max: 20 caractères
}

/**
 * ============================
 * Interface LoginDTO
 * Correspond EXACTEMENT au LoginDTO backend
 * Utilisée pour la connexion avec MetaMask (signature)
 * ============================
 */
export interface LoginDTO {
  walletAdresse: string; // Adresse wallet MetaMask
  signature: string;     // Signature du message d'authentification
}

/**
 * ============================
 * Interface JwtResponseDTO
 * Correspond EXACTEMENT au JwtResponseDTO backend
 * Réponse retournée après login/register réussi
 * ============================
 */
export interface JwtResponseDTO {
  token: string;         // Token JWT
  type: string;          // Toujours "Bearer"
  user: UserResponseDTO; // Informations utilisateur
}

/**
 * ============================
 * Interface EmailVerificationRequest
 * Pour demander l'envoi d'un email de vérification
 * ============================
 */
export interface EmailVerificationRequest {
  email: string;
}

/**
 * ============================
 * Interface EmailVerificationConfirm
 * Pour confirmer la vérification d'email avec le token
 * ============================
 */
export interface EmailVerificationConfirm {
  token: string;
}

/**
 * ============================
 * Interface EmailVerificationResponse
 * Réponse générique backend pour vérification email
 * ============================
 */
export interface EmailVerificationResponse {
  message: string;
}

/**
 * ============================
 * Interface ApiErrorResponse
 * Format standard des erreurs backend
 * ============================
 */
export interface ApiErrorResponse {
  message: string;
  status?: number;
  timestamp?: string;
  path?: string;
}

/**
 * ============================
 * Interface AuthState
 * État de l'authentification pour NgRx Store
 * ============================
 */
export interface AuthState {
  user: UserResponseDTO | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}
