// src/app/store/listings/listing.selectors.ts

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ListingsState } from './listing.reducer';
import { PropertyCard } from '../../core/models/property-card.model';
import { PropertySearchResultDTO } from '../../core/models/property.model';

/**
 * ============================
 * SÉLECTEUR RACINE
 * ============================
 */
export const selectListingsState = createFeatureSelector<ListingsState>('listings');

/**
 * ============================
 * SÉLECTEURS DE BASE
 * ============================
 */
export const selectAllProperties = createSelector(
  selectListingsState,
  (state: ListingsState) => state.allProperties  // ✅ Corrigé
);

export const selectSearchResults = createSelector(
  selectListingsState,
  (state: ListingsState) => state.searchResults
);

export const selectFilters = createSelector(
  selectListingsState,
  (state: ListingsState) => state.filters
);

export const selectListingsLoading = createSelector(
  selectListingsState,
  (state: ListingsState) => state.loading
);

export const selectListingsError = createSelector(
  selectListingsState,
  (state: ListingsState) => state.error
);

/**
 * ============================
 * SÉLECTEUR : LISTINGS AFFICHÉES
 * Retourne PropertyCard[] OU PropertySearchResultDTO[]
 * selon le mode (browse ou search)
 * ============================
 */
export const selectDisplayedListings = createSelector(
  selectListingsState,
  (state: ListingsState): (PropertyCard | PropertySearchResultDTO)[] => {
    // Si searchResults existe → mode search
    if (state.searchResults && state.searchResults.length > 0) {
      return state.searchResults;
    }
    // Sinon → mode browse
    return state.allProperties;  // ✅ Corrigé
  }
);

/**
 * ============================
 * SÉLECTEUR : Y A-T-IL DES RÉSULTATS DE RECHERCHE ?
 * ============================
 */
export const selectHasSearchResults = createSelector(
  selectListingsState,
  (state: ListingsState) => state.searchResults.length > 0
);

/**
 * ============================
 * SÉLECTEUR : NOMBRE TOTAL DE RÉSULTATS
 * ============================
 */
export const selectTotalResults = createSelector(
  selectDisplayedListings,
  (listings) => listings.length
);

/**
 * ============================
 * SÉLECTEUR : Y A-T-IL DES FILTRES ACTIFS ?
 * ============================
 */
export const selectHasActiveFilters = createSelector(
  selectFilters,
  (filters) => {
    return !!(
      filters.propertyType ||
      filters.placeType ||
      filters.minPrice ||
      filters.maxPrice ||
      filters.bedrooms ||
      filters.bathrooms ||
      filters.beds ||
      filters.instantBooking ||
      (filters.amenityIds && filters.amenityIds.length > 0) ||
      filters.smokingAllowed ||
      filters.eventsAllowed
    );
  }
);


/**
 * ============================
 * SÉLECTEUR : DATES BLOQUÉES
 * ============================
 */
export const selectPropertyBlockedDates = createSelector(
  selectListingsState,
  (state: ListingsState) => state.blockedDates
);

/**
 * ============================
 * SÉLECTEUR : SET DE DATES BLOQUÉES (pour le calendrier)
 * ============================
 */
export const selectPropertyBlockedDatesSet = createSelector(
  selectPropertyBlockedDates,
  (dates: string[]) => new Set(dates)
);





/**
 * ============================
 * SELECTORS MY PROPERTIES (HOST)
 * ============================
 */
export const selectMyProperties = createSelector(
  selectListingsState,
  (state: ListingsState): PropertyCard[] => state.myProperties
);

export const selectMyPropertiesLoaded = createSelector(
  selectListingsState,
  (state: ListingsState) => state.myPropertiesLoaded
);

export const selectMyPropertiesCount = createSelector(
  selectMyProperties,
  (properties) => properties.length
);

/**
 * ============================
 * SELECTOR : MY PROPERTIES BY STATUS
 * ============================
 */
export const selectMyPropertiesByStatus = (status: string) => createSelector(
  selectMyProperties,
  (properties) => properties.filter(p => p.status === status)
);

export const selectMyActiveProperties = createSelector(
  selectMyProperties,
  (properties) => properties.filter(p => p.status === 'ACTIVE')
);

export const selectMyDraftProperties = createSelector(
  selectMyProperties,
  (properties) => properties.filter(p => p.status === 'DRAFT')
);

export const selectMyArchivedProperties = createSelector(
  selectMyProperties,
  (properties) => properties.filter(p => p.status === 'ARCHIVED')
);
