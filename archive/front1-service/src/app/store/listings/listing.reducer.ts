// src/app/store/listings/listing.reducer.ts
import { createReducer, on } from '@ngrx/store';
import * as ListingsActions from './listing.actions';
import { Property, PropertySearchResultDTO, PropertySearchFilters } from '../../core/models/property.model';
import { PropertyCard } from '../../core/models/property-card.model';

/**
 * ============================
 * ÉTAT LISTINGS
 * ============================
 */
export interface ListingsState {
  // Browse/Search (existant)
  allProperties: PropertyCard[];
  searchResults: PropertySearchResultDTO[];
  filters: PropertySearchFilters;
  blockedDates: string[];

  // ✅ NOUVEAU : My Properties (Host)
  myProperties: PropertyCard[];
  myPropertiesLoaded: boolean;

  // Common
  loading: boolean;
  error: string | null;
  currentPage: number;
  totalPages: number;
  totalElements: number;
}

/**
 * ============================
 * ÉTAT INITIAL
 * ============================
 */
export const initialState: ListingsState = {
  allProperties: [],
  searchResults: [],
  filters: {
    adults: 1,
    children: 0,
    babies: 0,
    pets: 0
  },
  blockedDates: [],

  // ✅ NOUVEAU
  myProperties: [],
  myPropertiesLoaded: false,

  loading: false,
  error: null,
  currentPage: 0,
  totalPages: 0,
  totalElements: 0
};

/**
 * ============================
 * REDUCER
 * ============================
 */
export const listingsReducer = createReducer(
  initialState,

  // ========================================
  // LOAD ALL PROPERTIES (existant)
  // ========================================
  on(ListingsActions.loadAllProperties, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ListingsActions.loadAllPropertiesSuccess, (state, { properties }) => ({
    ...state,
    allProperties: properties,
    searchResults: [],
    loading: false
  })),

  on(ListingsActions.loadAllPropertiesFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // FILTER PROPERTIES (existant)
  // ========================================
  on(ListingsActions.filterProperties, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ListingsActions.filterPropertiesSuccess, (state, { properties }) => ({
    ...state,
    allProperties: properties,
    searchResults: [],
    loading: false
  })),

  on(ListingsActions.filterPropertiesFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // SEARCH PROPERTIES (existant)
  // ========================================
  on(ListingsActions.searchProperties, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ListingsActions.searchPropertiesSuccess, (state, { results }) => ({
    ...state,
    searchResults: results,
    allProperties: [],
    loading: false
  })),

  on(ListingsActions.searchPropertiesFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // UPDATE/CLEAR FILTERS (existant)
  // ========================================
  on(ListingsActions.updateFilters, (state, { filters }) => ({
    ...state,
    filters: { ...state.filters, ...filters }
  })),

  on(ListingsActions.clearFilters, (state) => ({
    ...state,
    filters: {
      adults: 1,
      children: 0,
      babies: 0,
      pets: 0
    },
    searchResults: []
  })),

  // ========================================
  // BLOCKED DATES (existant)
  // ========================================
  on(ListingsActions.loadPropertyBlockedDates, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ListingsActions.loadPropertyBlockedDatesSuccess, (state, { blockedDates }) => ({
    ...state,
    blockedDates,
    loading: false
  })),

  on(ListingsActions.loadPropertyBlockedDatesFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // ✅ NOUVEAU : MY PROPERTIES (HOST)
  // ========================================
  on(ListingsActions.loadMyProperties, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ListingsActions.loadMyPropertiesSuccess, (state, { properties }) => ({
    ...state,
    myProperties: properties,
    myPropertiesLoaded: true,
    loading: false
  })),

  on(ListingsActions.loadMyPropertiesFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // ✅ NOUVEAU : DELETE PROPERTY (HOST)
  // ========================================
  on(ListingsActions.deleteProperty, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ListingsActions.deletePropertySuccess, (state, { propertyId }) => ({
    ...state,
    myProperties: state.myProperties.filter(p => p.propertyId !== propertyId),
    loading: false
  })),

  on(ListingsActions.deletePropertyFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // ✅ NOUVEAU : PUBLISH PROPERTY (HOST)
  // ========================================
  on(ListingsActions.publishProperty, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(ListingsActions.publishPropertySuccess, (state, { property }) => ({
    ...state,
    myProperties: state.myProperties.map(p =>
      p.propertyId === property.propertyId ? property : p
    ),
    loading: false
  })),

  on(ListingsActions.publishPropertyFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // ========================================
  // CLEAR ERROR (existant)
  // ========================================
  on(ListingsActions.clearError, (state) => ({
    ...state,
    error: null
  }))
);
