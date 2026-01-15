// src/app/store/listings/listing.effects.ts

import {inject, Injectable} from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, exhaustMap, switchMap } from 'rxjs/operators';

import { PropertyService } from '../../core/services/property.service';
import * as ListingsActions from './listing.actions';

/**
 * ============================
 * LISTING EFFECTS
 * Gère les side effects (appels API) pour le store listings
 * ============================
 */
@Injectable()
export class ListingsEffects {
  private actions$ = inject(Actions);
  private propertyService = inject(PropertyService);

  /**
   * ============================
   * EFFECT: LOAD ALL PROPERTIES
   * Déclenché par: loadAllProperties
   * Appelle: getAllProperties() du service
   * ============================
   */
  loadAllProperties$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.loadAllProperties),
      exhaustMap(({ page = 0, size = 50 }) =>
        this.propertyService.getAllProperties(page, size).pipe(
          map(response => {
            // response est une Page<Property> avec { content: Property[], totalElements: number }
            const properties = response.content || [];
            const total = response.totalElements || 0;
            console.log(properties);
            return ListingsActions.loadAllPropertiesSuccess({
              properties,
              total
            });
          }),
          catchError(error => {
            console.error('Erreur loadAllProperties:', error);
            return of(ListingsActions.loadAllPropertiesFailure({
              error: error.message || 'Erreur lors du chargement des propriétés'
            }));
          })
        )
      )
    )
  );

  /**
   * ============================
   * EFFECT: SEARCH PROPERTIES (AVEC DATES)
   * Déclenché par: searchProperties
   * Appelle: searchProperties() du service
   * Utilisé quand on a checkIn/checkOut
   * ============================
   */
  searchProperties$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.searchProperties),
      exhaustMap(({ filters }) =>
        this.propertyService.searchProperties(filters).pipe(
          map(results => {
            console.log('✅ Résultats recherche (avec dates):', results.length);
            console.log(results);
            return ListingsActions.searchPropertiesSuccess({ results });
          }),
          catchError(error => {
            console.error('❌ Erreur searchProperties:', error);
            return of(ListingsActions.searchPropertiesFailure({
              error: error.message || 'Erreur lors de la recherche'
            }));
          })
        )
      )
    )
  );

  /**
   * ============================
   * EFFECT: FILTER PROPERTIES (SANS DATES)
   * Déclenché par: filterProperties
   * Appelle: filterProperties() du service
   * Utilisé quand on N'a PAS checkIn/checkOut
   * ============================
   */
  filterProperties$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.filterProperties),
      exhaustMap(({ filters }) =>
        this.propertyService.filterProperties(filters).pipe(
          map(properties => {
            console.log('✅ Résultats filtrage (sans dates):', properties.length);
            return ListingsActions.filterPropertiesSuccess({ properties });
          }),
          catchError(error => {
            console.error('❌ Erreur filterProperties:', error);
            return of(ListingsActions.filterPropertiesFailure({
              error: error.message || 'Erreur lors du filtrage'
            }));
          })
        )
      )
    )
  );

  /**
   * ============================
   * EFFECT: LOAD PROPERTY DETAIL
   * Déclenché par: loadPropertyDetail
   * Appelle: getPropertyById() du service
   * ============================
   */
  loadPropertyDetail$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.loadPropertyDetail),
      switchMap(({ id }) =>
        this.propertyService.getPropertyById(id).pipe(
          map(property => ListingsActions.loadPropertyDetailSuccess({ property })),
          catchError(error => {
            console.error('Erreur loadPropertyDetail:', error);
            return of(ListingsActions.loadPropertyDetailFailure({
              error: error.message || 'Erreur lors du chargement du détail'
            }));
          })
        )
      )
    )
  );






  loadPropertyBlockedDates$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.loadPropertyBlockedDates),
      exhaustMap(({ propertyId, start, end }) =>
        this.propertyService.getBlockedDates(propertyId, start, end).pipe(
          map(availabilities => {
            console.log('📅 Raw availabilities from backend:', availabilities);

            const blockedDates: string[] = [];

            availabilities.forEach((availability: any) => {
              if (!availability.isAvailable) {

                console.log(`🔍 Processing period:`, availability.dateDebut, '→', availability.dateFin);

                // ✅ Convertir en string si c'est un array
                const startStr = this.convertToDateString(availability.dateDebut);
                const endStr = this.convertToDateString(availability.dateFin);

                console.log(`   Converted: ${startStr} → ${endStr}`);

                // ✅ Générer les dates
                const dates = this.generateDateRange(startStr, endStr);

                dates.forEach(date => {
                  blockedDates.push(date);
                  console.log(`   ➕ ${date}`);
                });
              }
            });

            console.log('✅ Total blocked dates:', blockedDates.length);
            console.log('✅ Blocked dates:', blockedDates);

            return ListingsActions.loadPropertyBlockedDatesSuccess({ blockedDates });
          }),
          catchError(error => {
            console.error('❌ Error loading blocked dates:', error);
            return of(ListingsActions.loadPropertyBlockedDatesFailure({
              error: error.message || 'Erreur lors du chargement des dates bloquées'
            }));
          })
        )
      )
    )
  );

  /**
   * ✅ Convertir date en string
   * Accepte : "2026-01-03" OU [2026, 1, 3]
   * Retourne : "2026-01-03"
   */
  private convertToDateString(date: string | number[]): string {
    if (typeof date === 'string') {
      // Déjà au bon format
      return date;
    }

    if (Array.isArray(date)) {
      // Format array [year, month, day]
      const year = date[0];
      const month = String(date[1]).padStart(2, '0');
      const day = String(date[2]).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }

    throw new Error('Invalid date format');
  }

  /**
   * ✅ Générer toutes les dates entre start et end (INCLUS)
   * @param start Format: "2026-01-03"
   * @param end Format: "2026-01-05"
   * @returns ["2026-01-03", "2026-01-04", "2026-01-05"]
   */
  private generateDateRange(start: string, end: string): string[] {
    const dates: string[] = [];

    // Parser les dates
    const [startYear, startMonth, startDay] = start.split('-').map(Number);
    const [endYear, endMonth, endDay] = end.split('-').map(Number);

    let currentYear = startYear;
    let currentMonth = startMonth;
    let currentDay = startDay;

    // Boucle jusqu'à atteindre la date de fin
    while (
      currentYear < endYear ||
      (currentYear === endYear && currentMonth < endMonth) ||
      (currentYear === endYear && currentMonth === endMonth && currentDay <= endDay)
      ) {
      // Formater la date courante
      const dateStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(currentDay).padStart(2, '0')}`;
      dates.push(dateStr);

      // Passer au jour suivant
      currentDay++;

      // Gérer le passage au mois suivant
      const daysInMonth = new Date(currentYear, currentMonth, 0).getDate();
      if (currentDay > daysInMonth) {
        currentDay = 1;
        currentMonth++;

        // Gérer le passage à l'année suivante
        if (currentMonth > 12) {
          currentMonth = 1;
          currentYear++;
        }
      }
    }

    return dates;
  }

  /**
   * ============================
   * EFFECT: LOAD MY PROPERTIES (HOST)
   * ============================
   */
  loadMyProperties$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.loadMyProperties),
      exhaustMap(({ userId }) =>
        this.propertyService.getMyProperties(userId).pipe(
          map(properties => {
            console.log('✅ My properties loaded:', properties);

            return ListingsActions.loadMyPropertiesSuccess({ properties });
          }),
          catchError(error => {
            console.error('❌ Error loading my properties:', error);
            return of(ListingsActions.loadMyPropertiesFailure({
              error: error.message || 'Error loading your properties'
            }));
          })
        )
      )
    )
  );

  /**
   * ============================
   * EFFECT: DELETE PROPERTY (HOST)
   * ============================
   */
  deleteProperty$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.deleteProperty),
      exhaustMap(({ propertyId }) =>
        this.propertyService.deleteProperty(propertyId).pipe(
          map(() => {
            console.log('✅ Property deleted:', propertyId);
            return ListingsActions.deletePropertySuccess({ propertyId });
          }),
          catchError(error => {
            console.error('❌ Error deleting property:', error);
            return of(ListingsActions.deletePropertyFailure({
              error: error.message || 'Error deleting property'
            }));
          })
        )
      )
    )
  );

  /**
   * ============================
   * EFFECT: PUBLISH PROPERTY (HOST)
   * ============================
   */
  publishProperty$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.publishProperty),
      exhaustMap(({ propertyId }) =>
        this.propertyService.publishProperty(propertyId).pipe(
          map(property => {
            console.log('✅ Property published:', property);
            return ListingsActions.publishPropertySuccess({ property });
          }),
          catchError(error => {
            console.error('❌ Error publishing property:', error);
            return of(ListingsActions.publishPropertyFailure({
              error: error.message || 'Error publishing property'
            }));
          })
        )
      )
    )
  );

  /**
   * ============================
   * EFFECT: BLOCK DATES (HOST)
   * ============================
   */
  blockDates$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.blockDates),
      exhaustMap(({ propertyId, start, end, reason }) =>
        this.propertyService.blockDates(propertyId, start, end, reason).pipe(
          map(() => {
            console.log('✅ Dates blocked:', start, 'to', end);
            return ListingsActions.blockDatesSuccess({ propertyId, start, end });
          }),
          catchError(error => {
            console.error('❌ Error blocking dates:', error);
            return of(ListingsActions.blockDatesFailure({
              error: error.message || 'Error blocking dates'
            }));
          })
        )
      )
    )
  );

  /**
   * ============================
   * EFFECT: UNBLOCK DATES (HOST)
   * ============================
   */
  unblockDates$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ListingsActions.unblockDates),
      exhaustMap(({ propertyId, start, end }) =>
        this.propertyService.unblockDates(propertyId, start, end).pipe(
          map(() => {
            console.log('✅ Dates unblocked:', start, 'to', end);
            return ListingsActions.unblockDatesSuccess({ propertyId, start, end });
          }),
          catchError(error => {
            console.error('❌ Error unblocking dates:', error);
            return of(ListingsActions.unblockDatesFailure({
              error: error.message || 'Error unblocking dates'
            }));
          })
        )
      )
    )
  );
}
