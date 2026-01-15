import { createReducer, on } from '@ngrx/store';
import * as ReviewActions from './review.actions';
import {Review} from "../../core/models/review.model";
import {PropertyReviewStats} from "../../core/models/review-stats.model";

export interface ReviewState {
  reviews: Review[];
  stats: PropertyReviewStats | null;
  loading: boolean;
  error: string | null;
}

export const initialState: ReviewState = {
  reviews: [],
  stats: null,
  loading: false,
  error: null
};

export const reviewReducer = createReducer(
  initialState,

  // Load reviews
  on(ReviewActions.loadPropertyReviews, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ReviewActions.loadPropertyReviewsSuccess, (state, { reviews }) => ({
    ...state,
    reviews,
    loading: false
  })),

  on(ReviewActions.loadPropertyReviewsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Load stats
  on(ReviewActions.loadPropertyStatsSuccess, (state, { stats }) => ({
    ...state,
    stats
  })),

  // Create review
  on(ReviewActions.createReview, (state) => ({
    ...state,
    loading: true
  })),

  on(ReviewActions.createReviewSuccess, (state, { review }) => ({
    ...state,
    reviews: [review, ...state.reviews],
    loading: false
  })),

  // Delete review
  on(ReviewActions.deleteReviewSuccess, (state, { reviewId }) => ({
    ...state,
    reviews: state.reviews.filter(r => r.id !== reviewId)
  }))
);
