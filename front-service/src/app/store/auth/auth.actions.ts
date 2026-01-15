// src/app/store/auth/auth.actions.ts

import { createAction, props } from '@ngrx/store'; //props cad les donns que l'action transporte
import { RegisterDTO, LoginDTO, JwtResponseDTO } from '../../core/models/auth.model';
import { User } from '../../core/models/user.model';

/**
 * ============================
 * ACTIONS D'INSCRIPTION (REGISTER)
 * Flow : register → API call → registerSuccess OU registerFailure
 * ============================
 */

export const initAuth = createAction('[Auth] Init Auth');

export const initAuthSuccess = createAction(
  '[Auth] Init Auth Success',
  props<{ user: User; token: string }>()
);

export const initAuthFailure = createAction('[Auth] Init Auth Failure');

// Action déclenchée quand l'utilisateur soumet le formulaire d'inscription
export const register = createAction(
  '[Auth] Register',
  props<{ registerData: RegisterDTO }>()
);

// Action déclenchée si l'inscription réussit (backend retourne token + user)
export const registerSuccess = createAction(
  '[Auth] Register Success',
  props<{ response: JwtResponseDTO }>()
);

// Action déclenchée si l'inscription échoue (erreur backend)
export const registerFailure = createAction(
  '[Auth] Register Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS DE CONNEXION (LOGIN)
 * Flow : login → API call → loginSuccess OU loginFailure
 * ============================
 */

// Action déclenchée quand l'utilisateur se connecte avec MetaMask
export const login = createAction(
  '[Auth] Login',
  props<{ loginData: LoginDTO }>()
);

// Action déclenchée si la connexion réussit
export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ response: JwtResponseDTO }>()
);

// Action déclenchée si la connexion échoue
export const loginFailure = createAction(
  '[Auth] Login Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS DE CHARGEMENT UTILISATEUR
 * Flow : loadCurrentUser → API call → loadCurrentUserSuccess OU loadCurrentUserFailure
 * ============================
 */

// Action pour charger l'utilisateur actuel depuis le backend (GET /users/me)
export const loadCurrentUser = createAction('[Auth] Load Current User');

// Action déclenchée si le chargement réussit
export const loadCurrentUserSuccess = createAction(
  '[Auth] Load Current User Success',
  props<{ user: User }>()
);

// Action déclenchée si le chargement échoue
export const loadCurrentUserFailure = createAction(
  '[Auth] Load Current User Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS DE DÉCONNEXION (LOGOUT)
 * ============================
 */

// Action pour déconnecter l'utilisateur
export const logout = createAction('[Auth] Logout');

// Action déclenchée après la déconnexion (nettoyage terminé)
export const logoutSuccess = createAction('[Auth] Logout Success');

/**
 * ============================
 * ACTIONS DE VÉRIFICATION EMAIL
 * ============================
 */

// Demander l'envoi d'un email de vérification
export const requestEmailVerification = createAction(
  '[Auth] Request Email Verification',
  props<{ email: string }>()
);

// Email de vérification envoyé avec succès
export const requestEmailVerificationSuccess = createAction(
  '[Auth] Request Email Verification Success',
  props<{ message: string }>()
);

// Erreur lors de l'envoi de l'email de vérification
export const requestEmailVerificationFailure = createAction(
  '[Auth] Request Email Verification Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTION POUR EFFACER LES ERREURS
 * Utilisée pour réinitialiser les messages d'erreur dans l'UI
 * ============================
 */
export const clearError = createAction('[Auth] Clear Error');
