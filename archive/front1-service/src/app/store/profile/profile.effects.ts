// src/app/store/profile/profile.effects.ts
// ✅ VERSION AVEC DEBUG COMPLET

import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of, forkJoin } from 'rxjs';
import { map, catchError, switchMap, tap, withLatestFrom } from 'rxjs/operators';

import * as ProfileActions from './ profile.actions';

import { UserService } from '../../core/services/user.service';
import { ReviewService } from '../../core/services/review.service';
import { PropertyService } from '../../core/services/property.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class ProfileEffects {

  private actions$ = inject(Actions);
  private store = inject(Store);
  private userService = inject(UserService);
  private reviewService = inject(ReviewService);
  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);

  // ========================================
  // LOAD PROFILE
  // ========================================
  loadProfile$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProfileActions.loadProfile),
      tap(action => console.log('📥 Effect: loadProfile', action)),
      switchMap(({ userId }) =>
        this.userService.getUserById(userId).pipe(
          tap(user => console.log('✅ User loaded:', user)),
          map(user => ProfileActions.loadProfileSuccess({ user })),
          catchError(error => {
            console.error('❌ Error loading profile:', error);
            return of(ProfileActions.loadProfileFailure({
              error: error.message || 'Failed to load profile'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // UPDATE PROFILE
  // ========================================
  updateProfile$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProfileActions.updateProfile),
      tap(action => console.log('📝 Effect: updateProfile', action)),
      switchMap(({ userId, updateData }) =>
        this.userService.updateUser(userId, updateData).pipe(
          tap(user => {
            console.log('✅ Profile updated:', user);
            this.snackBar.open('Profile updated successfully', 'Close', { duration: 3000 });
          }),
          map(user => ProfileActions.updateProfileSuccess({ user })),
          catchError(error => {
            console.error('❌ Error updating profile:', error);
            this.snackBar.open('Error updating profile', 'Close', { duration: 3000 });
            return of(ProfileActions.updateProfileFailure({
              error: error.message || 'Failed to update profile'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // UPLOAD PHOTO
  // ========================================
  uploadPhoto$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProfileActions.uploadPhoto),
      tap(action => console.log('📷 Effect: uploadPhoto', action)),
      switchMap(({ userId, file }) =>
        this.userService.uploadProfilePhoto(userId, file).pipe(
          tap(response => {
            console.log('✅ Photo uploaded:', response);
            this.snackBar.open('Photo updated successfully', 'Close', { duration: 3000 });
          }),
          map(response => ProfileActions.uploadPhotoSuccess({
            photoUrl: response.photoUrl
          })),
          catchError(error => {
            console.error('❌ Error uploading photo:', error);
            this.snackBar.open('Error uploading photo', 'Close', { duration: 3000 });
            return of(ProfileActions.uploadPhotoFailure({
              error: error.message || 'Failed to upload photo'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // LOAD USER LANGUAGES
  // ========================================
  loadUserLanguages$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProfileActions.loadUserLanguages),
      tap(action => console.log('🌍 Effect: loadUserLanguages', action)),
      switchMap(({ userId }) =>
        this.userService.getUserLanguages(userId).pipe(
          tap(languages => console.log('✅ Languages loaded:', languages)),
          map(languages => ProfileActions.loadUserLanguagesSuccess({ languages })),
          catchError(error => {
            console.error('❌ Error loading languages:', error);
            return of(ProfileActions.loadUserLanguagesFailure({
              error: error.message || 'Failed to load languages'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // ADD LANGUAGE
  // ========================================
  addLanguage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProfileActions.addLanguage),
      tap(action => console.log('➕ Effect: addLanguage', action)),
      switchMap(({ userId, languageData }) => {
        console.log('📤 Calling API: POST /users/' + userId + '/languages');
        console.log('📦 Payload:', languageData);

        return this.userService.addLanguageToUser(userId, languageData).pipe(
          tap(language => {
            console.log('✅ Language added:', language);
            this.snackBar.open('Language added successfully', 'Close', { duration: 3000 });
          }),
          map(language => {
            console.log('📨 Dispatching addLanguageSuccess');
            return ProfileActions.addLanguageSuccess({ language });
          }),
          catchError(error => {
            console.error('❌ Error adding language:', error);
            console.error('Error details:', {
              status: error.status,
              message: error.message,
              error: error.error
            });
            this.snackBar.open('Error adding language', 'Close', { duration: 3000 });
            return of(ProfileActions.addLanguageFailure({
              error: error.message || 'Failed to add language'
            }));
          })
        );
      })
    )
  );

  // ========================================
  // REMOVE LANGUAGE
  // ========================================
  removeLanguage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProfileActions.removeLanguage),
      tap(action => console.log('🗑️ Effect: removeLanguage', action)),
      switchMap(({ userId, languageId }) =>
        this.userService.removeLanguageFromUser(userId, languageId).pipe(
          tap(() => {
            console.log('✅ Language removed');
            this.snackBar.open('Language removed successfully', 'Close', { duration: 3000 });
          }),
          map(() => ProfileActions.removeLanguageSuccess({ languageId })),
          catchError(error => {
            console.error('❌ Error removing language:', error);
            this.snackBar.open('Error removing language', 'Close', { duration: 3000 });
            return of(ProfileActions.removeLanguageFailure({
              error: error.message || 'Failed to remove language'
            }));
          })
        )
      )
    )
  );

  // ========================================
  // LOAD USER REVIEWS
  // ========================================
  loadUserReviews$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProfileActions.loadUserReviews),
      tap(action => console.log('⭐ Effect: loadUserReviews', action)),
      switchMap(({ userId }) =>
        this.reviewService.getUserReviews(userId).pipe(
          tap(reviews => console.log('✅ Reviews loaded:', reviews)),
          switchMap(reviews => {
            if (reviews.length === 0) {
              return of(ProfileActions.loadUserReviewsSuccess({ reviews: [] }));
            }



            const reviewsWithProperties$ = reviews.map(review =>
              this.propertyService.getPropertyDetails(review.propertyId).pipe(

                map(property => ({
                  ...review,
                  propertyTitle: property.title,
                  propertyCity: property.city,
                  propertyCountry: property.country,
                  propertyMainPhoto: property.photos && property.photos.length > 0
                    ? property.photos[0].photoUrl
                    : null,
                  propertyType: property.propertyType,
                  hostId: property.userId
                })),
                catchError(() => of({
                  ...review,
                  propertyTitle: 'Unknown Property',
                  propertyCity: '',
                  propertyCountry: '',
                  propertyMainPhoto: null,  // ✅ null est maintenant accepté
                  propertyType: 'UNKNOWN',
                  hostId: 0,
                  hostName: 'Unknown'
                }))
              )
            );

            return forkJoin(reviewsWithProperties$).pipe(
              tap(enrichedReviews => console.log('✅ Reviews with properties:', enrichedReviews)),
              map(enrichedReviews => ProfileActions.loadUserReviewsSuccess({
                reviews: enrichedReviews
              }))
            );
          }),
          catchError(error => {
            console.error('❌ Error loading reviews:', error);
            return of(ProfileActions.loadUserReviewsFailure({
              error: error.message || 'Failed to load reviews'
            }));
          })
        )
      )
    )
  );
}
