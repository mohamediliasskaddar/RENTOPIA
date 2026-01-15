// src/app/store/review/review.effects.ts
// ✅ VERSION CORRIGÉE - SANS CONSTRUCTOR

import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, switchMap, tap } from 'rxjs/operators';
import * as ReviewActions from './review.actions';
import { ReviewService } from '../../core/services/review.service';
import {WebSocketService} from "../../core/services/websocket.service";

@Injectable()
export class ReviewEffects {

  private actions$ = inject(Actions);
  private reviewService = inject(ReviewService);



  loadPropertyReviews$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ReviewActions.loadPropertyReviews),
      tap(action => console.log('🔥 Loading reviews for property:', action.propertyId)),
      switchMap(action =>
        this.reviewService.getPropertyReviews(action.propertyId).pipe(
          tap(reviews => console.log('✅ Reviews loaded:', reviews.length)),
          map(reviews => ReviewActions.loadPropertyReviewsSuccess({ reviews })),
          catchError(error => {
            console.error('❌ Error loading reviews:', error);
            return of(ReviewActions.loadPropertyReviewsFailure({
              error: error.message || 'Erreur lors du chargement des avis'
            }));
          })
        )
      )
    )
  );

  loadPropertyStats$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ReviewActions.loadPropertyStats),
      tap(action => console.log('🔥 Loading stats for property:', action.propertyId)),
      switchMap(action =>
        this.reviewService.getPropertyStats(action.propertyId).pipe(
          tap(stats => console.log('✅ Stats loaded:', stats)),
          map(stats => ReviewActions.loadPropertyStatsSuccess({ stats })),
          catchError(error => {
            console.error('❌ Error loading stats:', error);
            return of(ReviewActions.loadPropertyStatsFailure({
              error: error.message || 'Erreur lors du chargement des statistiques'
            }));
          })
        )
      )
    )
  );
}
