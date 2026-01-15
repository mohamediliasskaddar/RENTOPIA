// src/app/store/reviews/review.actions.ts
// ACTIONS COMPLÈTES - Vérifiez que TOUTES ces actions sont présentes

import { createAction, props } from '@ngrx/store';
import { Review, ReviewRequest, ReviewUpdateRequest } from '../../core/models/review.model';
import { PropertyReviewStats } from '../../core/models/review-stats.model';

// ==========================================
// CHARGER LES REVIEWS D'UNE PROPERTY
// ==========================================
export const loadPropertyReviews = createAction(
  '[Reviews] Load Property Reviews',
  props<{ propertyId: number }>()
);

export const loadPropertyReviewsSuccess = createAction(
  '[Reviews] Load Property Reviews Success',
  props<{ reviews: Review[] }>()
);

export const loadPropertyReviewsFailure = createAction(
  '[Reviews] Load Property Reviews Failure',
  props<{ error: string }>()
);

// ==========================================
// CHARGER LES STATISTIQUES
// ==========================================
export const loadPropertyStats = createAction(
  '[Reviews] Load Property Stats',
  props<{ propertyId: number }>()
);

export const loadPropertyStatsSuccess = createAction(
  '[Reviews] Load Property Stats Success',
  props<{ stats: PropertyReviewStats }>()
);

export const loadPropertyStatsFailure = createAction(
  '[Reviews] Load Property Stats Failure',
  props<{ error: string }>()
);

// ==========================================
// CRÉER UN REVIEW
// ==========================================
export const createReview = createAction(
  '[Reviews] Create Review',
  props<{ request: ReviewRequest }>()
);

export const createReviewSuccess = createAction(
  '[Reviews] Create Review Success',
  props<{ review: Review }>()
);

export const createReviewFailure = createAction(
  '[Reviews] Create Review Failure',
  props<{ error: string }>()
);

// ==========================================
// SUPPRIMER UN REVIEW
// ==========================================
export const deleteReview = createAction(
  '[Reviews] Delete Review',
  props<{ reviewId: number; userId: number }>()
);

export const deleteReviewSuccess = createAction(
  '[Reviews] Delete Review Success',
  props<{ reviewId: number }>()
);

export const deleteReviewFailure = createAction(
  '[Reviews] Delete Review Failure',
  props<{ error: string }>()
);

// ==========================================
// METTRE À JOUR UN REVIEW (optionnel pour lecture seule)
// ==========================================
export const updateReview = createAction(
  '[Reviews] Update Review',
  props<{ reviewId: number; request: ReviewUpdateRequest; userId: number }>()
);

export const updateReviewSuccess = createAction(
  '[Reviews] Update Review Success',
  props<{ review: Review }>()
);

export const updateReviewFailure = createAction(
  '[Reviews] Update Review Failure',
  props<{ error: string }>()
);
