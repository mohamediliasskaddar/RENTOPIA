// src/app/store/listings/listing.actions.ts

import { createAction, props } from '@ngrx/store';
import {
  Property,
  PropertySearchResultDTO,
  PropertySearchFilters
} from '../../core/models/property.model';
import {PropertyCard} from "../../core/models/property-card.model";

/**
 * ============================
 * ACTIONS LOAD ALL PROPERTIES (Page /listings sans filtres)
 * ============================
 */
export const loadAllProperties = createAction(
  '[Listings] Load All Properties',
  props<{ page: number; size: number }>()
);

export const loadAllPropertiesSuccess = createAction(
  '[Listings] Load All Properties Success',
  props<{ properties: PropertyCard[]; total: number }>()  // ✅ PropertyCard au lieu de Property
);

export const loadAllPropertiesFailure = createAction(
  '[Listings] Load All Properties Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS SEARCH PROPERTIES (Page /listings avec filtres + DATES)
 * ============================
 */
export const searchProperties = createAction(
  '[Listings] Search Properties',
  props<{ filters: PropertySearchFilters }>()
);

export const searchPropertiesSuccess = createAction(
  '[Listings] Search Properties Success',
  props<{ results: PropertySearchResultDTO[] }>()
);

export const searchPropertiesFailure = createAction(
  '[Listings] Search Properties Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS FILTER PROPERTIES (Page /listings avec filtres SANS dates)
 * ============================
 */
export const filterProperties = createAction(
  '[Listings] Filter Properties',
  props<{ filters: PropertySearchFilters }>()
);

export const filterPropertiesSuccess = createAction(
  '[Listings] Filter Properties Success',
  props<{ properties: PropertyCard[] }>()  // ✅ PropertyCard au lieu de Property
);

export const filterPropertiesFailure = createAction(
  '[Listings] Filter Properties Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS LOAD PROPERTY DETAIL
 * ============================
 */
export const loadPropertyDetail = createAction(
  '[Listings] Load Property Detail',
  props<{ id: number }>()
);

export const loadPropertyDetailSuccess = createAction(
  '[Listings] Load Property Detail Success',
  props<{ property: Property }>()
);

export const loadPropertyDetailFailure = createAction(
  '[Listings] Load Property Detail Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS UPDATE FILTERS
 * ============================
 */
export const updateFilters = createAction(
  '[Listings] Update Filters',
  props<{ filters: Partial<PropertySearchFilters> }>()
);

export const clearFilters = createAction('[Listings] Clear Filters');

/**
 * ============================
 * ACTIONS CLEAR ERRORS
 * ============================
 */
export const clearError = createAction('[Listings] Clear Error');



export const loadPropertyBlockedDates = createAction(
  '[Listings] Load Property Blocked Dates',
  props<{ propertyId: number; start: string; end: string }>()
);

export const loadPropertyBlockedDatesSuccess = createAction(
  '[Listings] Load Property Blocked Dates Success',
  props<{ blockedDates: string[] }>()
);

export const loadPropertyBlockedDatesFailure = createAction(
  '[Listings] Load Property Blocked Dates Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS MY PROPERTIES (HOST)
 * ============================
 */
export const loadMyProperties = createAction(
  '[Host] Load My Properties',
  props<{ userId: number }>()
);

export const loadMyPropertiesSuccess = createAction(
  '[Host] Load My Properties Success',
  props<{ properties: PropertyCard[] }>()
);
export const loadMyPropertiesFailure = createAction(
  '[Host] Load My Properties Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS DELETE PROPERTY (HOST)
 * ============================
 */
export const deleteProperty = createAction(
  '[Host] Delete Property',
  props<{ propertyId: number }>()
);

export const deletePropertySuccess = createAction(
  '[Host] Delete Property Success',
  props<{ propertyId: number }>()
);

export const deletePropertyFailure = createAction(
  '[Host] Delete Property Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS PUBLISH PROPERTY (HOST)
 * ============================
 */
export const publishProperty = createAction(
  '[Host] Publish Property',
  props<{ propertyId: number }>()
);

export const publishPropertySuccess = createAction(
  '[Host] Publish Property Success',
  props<{ property: Property }>()
);

export const publishPropertyFailure = createAction(
  '[Host] Publish Property Failure',
  props<{ error: string }>()
);

/**
 * ============================
 * ACTIONS BLOCK/UNBLOCK DATES (HOST)
 * ============================
 */
export const blockDates = createAction(
  '[Host] Block Dates',
  props<{ propertyId: number; start: string; end: string; reason: string }>()
);

export const blockDatesSuccess = createAction(
  '[Host] Block Dates Success',
  props<{ propertyId: number; start: string; end: string }>()
);

export const blockDatesFailure = createAction(
  '[Host] Block Dates Failure',
  props<{ error: string }>()
);

export const unblockDates = createAction(
  '[Host] Unblock Dates',
  props<{ propertyId: number; start: string; end: string }>()
);

export const unblockDatesSuccess = createAction(
  '[Host] Unblock Dates Success',
  props<{ propertyId: number; start: string; end: string }>()
);

export const unblockDatesFailure = createAction(
  '[Host] Unblock Dates Failure',
  props<{ error: string }>()
);
