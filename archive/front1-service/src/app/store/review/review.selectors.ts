import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ReviewState } from './review.reducer';

export const selectReviewState = createFeatureSelector<ReviewState>('reviews');

export const selectAllReviews = createSelector(
  selectReviewState,
  (state) => state.reviews
);

export const selectReviewStats = createSelector(
  selectReviewState,
  (state) => state.stats
);

export const selectReviewsLoading = createSelector(
  selectReviewState,
  (state) => state.loading
);

export const selectReviewsError = createSelector(
  selectReviewState,
  (state) => state.error
);
