// src/app/store/profile/profile.selectors.ts

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ProfileState } from './profile.reducer';

/**
 * ============================
 * PROFILE SELECTORS
 * ============================
 */

// Sélecteur racine
export const selectProfileState = createFeatureSelector<ProfileState>('profile');

// ========================================
// SÉLECTEURS DE BASE
// ========================================

export const selectProfileUser = createSelector(
  selectProfileState,
  (state: ProfileState) => state.user
);

export const selectUserLanguages = createSelector(
  selectProfileState,
  (state: ProfileState) => state.languages
);

export const selectUserReviews = createSelector(
  selectProfileState,
  (state: ProfileState) => state.reviews
);

export const selectIsEditMode = createSelector(
  selectProfileState,
  (state: ProfileState) => state.isEditMode
);

export const selectProfileLoading = createSelector(
  selectProfileState,
  (state: ProfileState) => state.loading
);

export const selectUploadingPhoto = createSelector(
  selectProfileState,
  (state: ProfileState) => state.uploadingPhoto
);

export const selectProfileError = createSelector(
  selectProfileState,
  (state: ProfileState) => state.error
);

// ========================================
// SÉLECTEURS DÉRIVÉS
// ========================================

/**
 * Nom complet de l'utilisateur
 */
export const selectUserFullName = createSelector(
  selectProfileUser,
  (user) => user ? `${user.prenom} ${user.nom}` : ''
);

/**
 * Initiale pour l'avatar
 */
export const selectUserInitial = createSelector(
  selectProfileUser,
  (user) => user ? user.prenom.charAt(0).toUpperCase() : ''
);

/**
 * Est-ce que l'utilisateur a vérifié son email ?
 */
export const selectEmailVerified = createSelector(
  selectProfileUser,
  (user) => user?.emailVerified || false
);

/**
 * Est-ce que l'utilisateur a vérifié son téléphone ?
 */
export const selectPhoneVerified = createSelector(
  selectProfileUser,
  (user) => user?.telephoneVerified || false
);

/**
 * Nombre de langues parlées
 */
export const selectLanguagesCount = createSelector(
  selectUserLanguages,
  (languages) => languages.length
);

/**
 * Nombre d'avis donnés
 */
export const selectReviewsCount = createSelector(
  selectUserReviews,
  (reviews) => reviews.length
);

/**
 * Avis visibles seulement
 */
export const selectVisibleReviews = createSelector(
  selectUserReviews,
  (reviews) => reviews.filter(r => r.isVisible)
);

/**
 * Note moyenne des avis donnés
 */
export const selectAverageRating = createSelector(
  selectUserReviews,
  (reviews) => {
    if (reviews.length === 0) return 0;
    const sum = reviews.reduce((acc, review) => acc + review.ratingValue, 0);
    return Math.round((sum / reviews.length) * 10) / 10;
  }
);

/**
 * Année de création du compte
 */
export const selectMemberSince = createSelector(
  selectProfileUser,
  (user) => {
    if (!user?.createdAt) return '';
    return new Date(user.createdAt).getFullYear().toString();
  }
);

/**
 * Vérifier si le profil peut être édité
 */
export const selectCanEdit = createSelector(
  selectProfileLoading,
  selectUploadingPhoto,
  (loading, uploading) => !loading && !uploading
);
