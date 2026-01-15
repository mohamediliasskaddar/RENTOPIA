// src/app/store/profile/profile.actions.ts

import { createAction, props } from '@ngrx/store';
import { UserResponseDTO, UpdateUserDTO, UserLanguageDTO, AddLanguageDTO } from '../../core/models/user.model';
import { ReviewWithProperty } from '../../core/models/review.model';

/**
 * ============================
 * ACTIONS LOAD PROFILE
 * ============================
 */
export const loadProfile = createAction(
  '[Profile] Load Profile',
  props<{ userId: number }>()
);

export const loadProfileSuccess = createAction(
  '[Profile] Load Profile Success',
  props<{ user: UserResponseDTO }>()
);

export const loadProfileFailure = createAction(
  '[Profile] Load Profile Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS UPDATE PROFILE
 * ============================
 */
export const updateProfile = createAction(
  '[Profile] Update Profile',
  props<{ userId: number; updateData: UpdateUserDTO }>()
);

export const updateProfileSuccess = createAction(
  '[Profile] Update Profile Success',
  props<{ user: UserResponseDTO }>()
);

export const updateProfileFailure = createAction(
  '[Profile] Update Profile Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS UPLOAD PHOTO
 * ============================
 */
export const uploadPhoto = createAction(
  '[Profile] Upload Photo',
  props<{ userId: number; file: File }>()
);

export const uploadPhotoSuccess = createAction(
  '[Profile] Upload Photo Success',
  props<{ photoUrl: string }>()
);

export const uploadPhotoFailure = createAction(
  '[Profile] Upload Photo Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS MANAGE LANGUAGES
 * ============================
 */
export const loadUserLanguages = createAction(
  '[Profile] Load User Languages',
  props<{ userId: number }>()
);

export const loadUserLanguagesSuccess = createAction(
  '[Profile] Load User Languages Success',
  props<{ languages: UserLanguageDTO[] }>()
);

export const loadUserLanguagesFailure = createAction(
  '[Profile] Load User Languages Failure',
  props<{ error: string }>()
);

export const addLanguage = createAction(
  '[Profile] Add Language',
  props<{ userId: number; languageData: AddLanguageDTO }>()
);

export const addLanguageSuccess = createAction(
  '[Profile] Add Language Success',
  props<{ language: UserLanguageDTO }>()
);

export const addLanguageFailure = createAction(
  '[Profile] Add Language Failure',
  props<{ error: string }>()
);

export const removeLanguage = createAction(
  '[Profile] Remove Language',
  props<{ userId: number; languageId: number }>()
);

export const removeLanguageSuccess = createAction(
  '[Profile] Remove Language Success',
  props<{ languageId: number }>()
);

export const removeLanguageFailure = createAction(
  '[Profile] Remove Language Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS LOAD USER REVIEWS
 * ============================
 */
export const loadUserReviews = createAction(
  '[Profile] Load User Reviews',
  props<{ userId: number }>()
);

export const loadUserReviewsSuccess = createAction(
  '[Profile] Load User Reviews Success',
  props<{ reviews: ReviewWithProperty[] }>()
);

export const loadUserReviewsFailure = createAction(
  '[Profile] Load User Reviews Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS UI
 * ============================
 */
export const toggleEditMode = createAction('[Profile] Toggle Edit Mode');

export const clearError = createAction('[Profile] Clear Error');

export const resetProfileState = createAction('[Profile] Reset Profile State');
