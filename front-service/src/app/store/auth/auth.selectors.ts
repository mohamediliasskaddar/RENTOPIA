// src/app/store/auth/auth.selectors.ts

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthState } from './auth.reducer';
//createFeatureSelector : permet de récupérer une partie du store
// createSelector : permet de créer un selector personnalisé
// AuthState :  type de l’état auth (défini dans le reducer)

/**
 * ============================
 * SELECTORS D'AUTHENTIFICATION
 * Permettent d'extraire des morceaux spécifiques de l'état auth
 *
 * Avantages :
 * - Performance : mémoisation automatique (recalcul seulement si l'état change)
 * - Simplicité : logique centralisée pour accéder aux données
 * - Testabilité : facile à tester
 *
 * Utilisation dans un composant :
 * this.store.select(selectCurrentUser).subscribe(user => ...)
 * ============================
 */



/**
 * Ce selector récupère toute la partie “auth” du store
 */
export const selectAuthState = createFeatureSelector<AuthState>('auth');



// Maintenant on crée des selectors personnalisés pour prendre JUSTE ce qu’on veut du state auth




/**
 * ============================
 * SELECT CURRENT USER seulement
 * Retourne l'objet User complet ou null
 * ============================
 */
export const selectCurrentUser = createSelector(
  selectAuthState,
  (state: AuthState) => state.user
);

/**
 * ============================
 * SÉLECTEUR : Est-ce que l'utilisateur est authentifié ?
 * Retourne true/false
 * ============================
 */
export const selectIsAuthenticated = createSelector(
  selectAuthState,
  (state: AuthState) => state.isAuthenticated
);

/**
 * ============================
 * SÉLECTEUR : Loading (requête en cours)
 * Retourne true/false
 * Utilisé pour afficher des spinners
 * ============================
 */
export const selectAuthLoading = createSelector(
  selectAuthState,
  (state: AuthState) => state.loading
);

/**
 * ============================
 * SÉLECTEUR : Message d'erreur
 * Retourne le message d'erreur ou null
 * Utilisé pour afficher des alertes d'erreur
 * ============================
 */
export const selectAuthError = createSelector(
  selectAuthState,
  (state: AuthState) => state.error
);

/**
 * ============================
 * SÉLECTEUR : Message de vérification d'email
 * Retourne le message de confirmation ou null
 * ============================
 */
export const selectEmailVerificationMessage = createSelector(
  selectAuthState,
  (state: AuthState) => state.emailVerificationMessage
);

/**
 * ============================
 * SÉLECTEUR : Est-ce que l'utilisateur est un Host (propriétaire) ?
 * Retourne true/false
 * ============================
 */
export const selectIsHost = createSelector(
  selectCurrentUser,
  (user) => user?.isHost || false
);

/**
 * ============================
 * SÉLECTEUR : Est-ce que l'utilisateur est un Guest (locataire) ?
 * Retourne true/false
 * ============================
 */
export const selectIsGuest = createSelector(
  selectCurrentUser,
  (user) => user?.isGuest || false
);

/**
 * ============================
 * SÉLECTEUR : Adresse wallet de l'utilisateur
 * Retourne l'adresse wallet ou null
 * ============================
 */
export const selectUserWallet = createSelector(
  selectCurrentUser,
  (user) => user?.walletAdresse || null
);

/**
 * ============================
 * SÉLECTEUR : Email vérifié ?
 * Retourne true/false
 * ============================
 */
export const selectEmailVerified = createSelector(
  selectCurrentUser,
  (user) => user?.emailVerified || false
);

/**
 * ============================
 * SÉLECTEUR : Nom complet de l'utilisateur
 * Retourne "Prénom Nom" ou null
 * ============================
 */
export const selectUserFullName = createSelector(
  selectCurrentUser,
  (user) => user ? `${user.prenom} ${user.nom}` : null
);

/**
 * ============================
 * SÉLECTEUR : Langues parlées par l'utilisateur
 * Retourne la liste des langues ou []
 * ============================
 */
export const selectUserLanguages = createSelector(
  selectCurrentUser,
  (user) => user?.languages || []
);
export const selectUserId = createSelector(
  selectCurrentUser,
  (user) => user?.id ?? null
);
