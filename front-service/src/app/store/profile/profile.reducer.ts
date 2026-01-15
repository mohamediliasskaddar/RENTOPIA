// src/app/store/profile/profile.reducer.ts

import { createReducer, on } from '@ngrx/store';
import * as ProfileActions from './ profile.actions';
import { UserResponseDTO, UserLanguageDTO } from '../../core/models/user.model';
import { ReviewWithProperty } from '../../core/models/review.model';

/**
 * ============================
 * PROFILE STATE
 * ============================
 */
export interface ProfileState {
  user: UserResponseDTO | null;
  languages: UserLanguageDTO[];
  reviews: ReviewWithProperty[];

  // UI State
  isEditMode: boolean;
  loading: boolean;
  uploadingPhoto: boolean;
  error: string | null;
}

/**
 * ============================
 * INITIAL STATE
 * ============================
 */
export const initialState: ProfileState = {
  user: null,
  languages: [],
  reviews: [],

  isEditMode: false,
  loading: false,
  uploadingPhoto: false,
  error: null
};

/**
 * ============================
 * REDUCER
 * ============================
 */
export const profileReducer = createReducer(
  initialState,

  // ========================================
  // LOAD PROFILE
  // ========================================
  on(ProfileActions.loadProfile, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ProfileActions.loadProfileSuccess, (state, { user }) => ({
    ...state,
    user,
    loading: false
  })),

  on(ProfileActions.loadProfileFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // UPDATE PROFILE
  // ========================================
  on(ProfileActions.updateProfile, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ProfileActions.updateProfileSuccess, (state, { user }) => ({
    ...state,
    user,
    loading: false,
    isEditMode: false  // ✅ Sortir du mode édition après succès
  })),

  on(ProfileActions.updateProfileFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // UPLOAD PHOTO
  // ========================================
  on(ProfileActions.uploadPhoto, (state) => ({
    ...state,
    uploadingPhoto: true,
    error: null
  })),

  on(ProfileActions.uploadPhotoSuccess, (state, { photoUrl }) => ({
    ...state,
    user: state.user ? { ...state.user, photoUrl } : null,
    uploadingPhoto: false
  })),

  on(ProfileActions.uploadPhotoFailure, (state, { error }) => ({
    ...state,
    uploadingPhoto: false,
    error
  })),

  // ========================================
  // MANAGE LANGUAGES
  // ========================================
  on(ProfileActions.loadUserLanguages, (state) => ({
    ...state,
    loading: true
  })),

  on(ProfileActions.loadUserLanguagesSuccess, (state, { languages }) => ({
    ...state,
    languages,
    loading: false
  })),

  on(ProfileActions.loadUserLanguagesFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  on(ProfileActions.addLanguageSuccess, (state, { language }) => ({
    ...state,
    languages: [...state.languages, language]
  })),

  on(ProfileActions.removeLanguageSuccess, (state, { languageId }) => ({
    ...state,
    languages: state.languages.filter(l => l.languageId !== languageId)
  })),

  // ========================================
  // LOAD USER REVIEWS
  // ========================================
  on(ProfileActions.loadUserReviews, (state) => ({
    ...state,
    loading: true
  })),

  on(ProfileActions.loadUserReviewsSuccess, (state, { reviews }) => ({
    ...state,
    reviews,
    loading: false
  })),

  on(ProfileActions.loadUserReviewsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // UI ACTIONS
  // ========================================
  on(ProfileActions.toggleEditMode, (state) => ({
    ...state,
    isEditMode: !state.isEditMode,
    error: null
  })),

  on(ProfileActions.clearError, (state) => ({
    ...state,
    error: null
  })),

  on(ProfileActions.resetProfileState, () => initialState)
);
