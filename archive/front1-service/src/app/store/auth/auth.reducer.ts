// src/app/store/auth/auth.reducer.ts

import { createReducer, on } from '@ngrx/store';
import * as AuthActions from './auth.actions';
import { User } from '../../core/models/user.model';




/**
 * ÉTAT D'AUTHENTIFICATION (AuthState)
 */
export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
  emailVerificationMessage: string | null;
}

/**
 * ÉTAT INITIAL : les valeurs initiales avant que l'utilisateur fasse login.
 */
export const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  loading: false,
  error: null,
  emailVerificationMessage: null
};

/**
 * REDUCER
 */
export const authReducer = createReducer(
  initialState,
  // On dit au reducer , Quand cette action arrive =>  voici comment tu dois modifier le state."


  // ========================================
// INIT AUTH (RESTAURATION AU DÉMARRAGE)
// ========================================

// Quand on initialise l'auth au démarrage
  on(AuthActions.initAuth, (state) => ({
    ...state,
    loading: true
  })),

// Quand l'initialisation réussit (token valide + user chargé)
  on(AuthActions.initAuthSuccess, (state, { user, token }) => ({
    ...state,
    user,
    isAuthenticated: true,
    loading: false,
    error: null
  })),

// Quand l'initialisation échoue (pas de token ou token invalide)
  on(AuthActions.initAuthFailure, (state) => ({
    ...state,
    user: null,
    isAuthenticated: false,
    loading: false
  })),



  // ========================================
  // REGISTER (INSCRIPTION)
  // ========================================

  // Quand l'utilisateur soumet le formulaire d'inscription
  on(AuthActions.register, (state) => ({
    ...state,
    loading: true,  // Afficher le spinner
    error: null     // Effacer les anciennes erreurs
  })),

  // Quand l'inscription réussit
  on(AuthActions.registerSuccess, (state, { response }) => ({
    ...state,
    user: response.user as User,  // Sauvegarder l'utilisateur
    isAuthenticated: true,         // Marquer comme connecté
    loading: false,                // Cacher le spinner
    error: null
  })),

  // Quand l'inscription échoue
  on(AuthActions.registerFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error  // Afficher le message d'erreur
  })),

  // ========================================
  // LOGIN (CONNEXION)
  // ========================================

  // Quand l'utilisateur se connecte
  on(AuthActions.login, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  // Quand la connexion réussit
  on(AuthActions.loginSuccess, (state, { response }) => ({
    ...state,
    user: response.user as User,
    isAuthenticated: true,
    loading: false,
    error: null
  })),

  // Quand la connexion échoue
  on(AuthActions.loginFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error //  { error } cad objet { error: 'erreur...' } donc error <=> error: 'erreur...'
  })),

  // ========================================
  // LOAD USER (CHARGER UTILISATEUR)
  // ========================================

  // Quand on charge l'utilisateur depuis le backend
  on(AuthActions.loadCurrentUser, (state) => ({
    ...state,
    loading: true
  })),

  // Quand le chargement réussit
  on(AuthActions.loadCurrentUserSuccess, (state, { user }) => ({
    ...state,
    user,
    isAuthenticated: true,
    loading: false
  })),

  // Quand le chargement échoue
  on(AuthActions.loadCurrentUserFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // LOGOUT (DÉCONNEXION)
  // ========================================

  // Quand l'utilisateur se déconnecte → Réinitialiser tout
  on(AuthActions.logout, () => initialState),
  on(AuthActions.logoutSuccess, () => initialState),

  // ========================================
  // EMAIL VERIFICATION
  // ========================================

  // Quand on demande l'envoi d'un email de vérification
  on(AuthActions.requestEmailVerification, (state) => ({
    ...state,
    loading: true,
    emailVerificationMessage: null,
    error: null
  })),

  // Email envoyé avec succès
  on(AuthActions.requestEmailVerificationSuccess, (state, { message }) => ({
    ...state,
    loading: false,
    emailVerificationMessage: message
  })),

  // Erreur lors de l'envoi
  on(AuthActions.requestEmailVerificationFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // CLEAR ERROR (EFFACER LES ERREURS)
  // ========================================

  // Effacer les messages d'erreur et de vérification
  on(AuthActions.clearError, (state) => ({
    ...state,
    error: null,
    emailVerificationMessage: null
  }))
);
