// src/app/features/listings/listings.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil } from 'rxjs';

// Material Imports
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';

// Components
import { PropertyCardComponent } from '../property-card/property-card.component';
import { FiltersModalComponent } from '../filters-modal/filters-modal.component';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';

// Store
import * as ListingsActions from '../../store/listings/listing.actions';
import {
  selectDisplayedListings,
  selectListingsLoading,
  selectListingsError,
  selectFilters,
  selectHasSearchResults,

} from '../../store/listings/listing.selectors';

// Models
import { PropertySearchFilters } from '../../core/models/property.model';
import {take} from "rxjs/operators";

/**
 * ============================
 * COMPOSANT LISTINGS
 * Affiche la liste des properties avec filtres
 * Mode 1: Direct (/listings) → affiche properties avec prix par nuit
 * Mode 2: Depuis search bar → affiche properties avec prix total calculé
 * ============================
 */


@Component({
  selector: 'app-listings',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatChipsModule,
    PropertyCardComponent,
    SearchBarComponent
  ],
  templateUrl: './listings.component.html',
  styleUrl: './listings.component.scss'
})
export class ListingsComponent implements OnInit, OnDestroy {

  // Observables du store
  listings$: Observable<any[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  filters$: Observable<PropertySearchFilters>;
  hasSearchResults$: Observable<boolean>;

  // État local
  isSearchMode = false; // true si venu depuis search bar
  activeFiltersCount = 0;
  currentQueryParams: any = {}; // ✅ Stocker les query params

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog
  ) {
    this.listings$ = this.store.select(selectDisplayedListings);
    this.loading$ = this.store.select(selectListingsLoading);
    this.error$ = this.store.select(selectListingsError);
    this.filters$ = this.store.select(selectFilters);
    this.hasSearchResults$ = this.store.select(selectHasSearchResults);
  }

  ngOnInit(): void {
    // Écouter les query params (venant de search bar)
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.currentQueryParams = { ...params }; // ✅ Sauvegarder les params

      if (this.hasSearchParams(params)) {
        // Mode recherche : avec filtres
        this.isSearchMode = true;
        const filters = this.buildFiltersFromParams(params);
        this.store.dispatch(ListingsActions.searchProperties({ filters }));
      } else {
        // Mode direct : liste complète
        this.isSearchMode = false;
        this.store.dispatch(ListingsActions.loadAllProperties({ page: 0, size: 50 }));
      }
    });

    // Compter les filtres actifs
    this.filters$.pipe(takeUntil(this.destroy$)).subscribe(filters => {
      this.activeFiltersCount = this.countActiveFilters(filters);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * ============================
   * VÉRIFIER SI QUERY PARAMS PRÉSENTS
   * ============================
   */
  private hasSearchParams(params: any): boolean {
    return !!(params.city || params.country || params.checkIn || params.checkOut);
  }

  /**
   * ============================
   * CONSTRUIRE FILTRES DEPUIS QUERY PARAMS
   * ============================
   */
  private buildFiltersFromParams(params: any): PropertySearchFilters {
    const filters: PropertySearchFilters = {
      adults: parseInt(params.adults) || 1,
      children: parseInt(params.children) || 0,
      babies: parseInt(params.babies) || 0,
      pets: parseInt(params.pets) || 0
    };

    // Parser city et country
    if (params.city) filters.city = params.city;
    if (params.country) filters.country = params.country;
    if (params.checkIn) filters.checkIn = params.checkIn;
    if (params.checkOut) filters.checkOut = params.checkOut;

    return filters;
  }

  /**
   * ============================
   * COMPTER LES FILTRES ACTIFS
   * ============================
   */
  private countActiveFilters(filters: PropertySearchFilters): number {
    let count = 0;
    if (filters.propertyType) count++;
    if (filters.minPrice || filters.maxPrice) count++;
    if (filters.bedrooms) count++;
    if (filters.bathrooms) count++;
    if (filters.beds) count++;
    if (filters.amenityIds && filters.amenityIds.length > 0) count += filters.amenityIds.length;
    if (filters.instantBooking) count++;
    if (filters.smokingAllowed) count++;
    if (filters.eventsAllowed) count++;
    return count;
  }

  /**
   * ============================
   * OUVRIR LE MODAL DE FILTRES
   * ============================
   */
  openFiltersModal(): void {
    // ✅ Récupérer les filtres actuels de manière synchrone
    let currentFilters: PropertySearchFilters = {
      adults: 1,
      children: 0,
      babies: 0,
      pets: 0
    };

    // ✅ Utiliser take(1) pour obtenir une seule valeur
    this.filters$.pipe(take(1)).subscribe(filters => {
      currentFilters = { ...filters };
    });

    const dialogRef = this.dialog.open(FiltersModalComponent, {
      width: '680px',              // Largeur fixe professionnelle
      maxWidth: '90vw',            // Max 90% de la largeur sur mobile
      maxHeight: '90vh',           // Max 90% de la hauteur
      panelClass: 'filters-modal-container',  // ✅ Classe custom
      backdropClass: 'filters-modal-backdrop', // ✅ Backdrop custom
      disableClose: false,         // Peut fermer en cliquant dehors
      autoFocus: false,
      data: {
        currentFilters: currentFilters // ✅ Passer les filtres actuels
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('✅ Filtres reçus du modal:', result);
        this.applyFilters(result);
      }
    });
  }



  /**
   * ============================
   * APPLIQUER LES FILTRES
   * ============================
   */
  applyFilters(filters: PropertySearchFilters): void {
    console.log('🔍 Application des filtres...', filters);

    // ✅ Fusionner avec les filtres de recherche existants (dates, location, guests)
    const finalFilters: PropertySearchFilters = {
      ...filters
    };

    // Si on est en mode recherche, garder les dates et location
    if (this.isSearchMode && this.currentQueryParams) {
      if (this.currentQueryParams.city) finalFilters.city = this.currentQueryParams.city;
      if (this.currentQueryParams.country) finalFilters.country = this.currentQueryParams.country;
      if (this.currentQueryParams.checkIn) finalFilters.checkIn = this.currentQueryParams.checkIn;
      if (this.currentQueryParams.checkOut) finalFilters.checkOut = this.currentQueryParams.checkOut;
      if (this.currentQueryParams.adults) finalFilters.adults = parseInt(this.currentQueryParams.adults);
      if (this.currentQueryParams.children) finalFilters.children = parseInt(this.currentQueryParams.children);
      if (this.currentQueryParams.babies) finalFilters.babies = parseInt(this.currentQueryParams.babies);
      if (this.currentQueryParams.pets) finalFilters.pets = parseInt(this.currentQueryParams.pets);
    }

    console.log('🎯 Filtres finaux à appliquer:', finalFilters);

    // ✅ Mettre à jour les filtres dans le store
    this.store.dispatch(ListingsActions.updateFilters({ filters: finalFilters }));
    if (finalFilters.checkIn && finalFilters.checkOut) {
      this.store.dispatch(ListingsActions.searchProperties({ filters: finalFilters }));
    } else {
      this.store.dispatch(ListingsActions.filterProperties({ filters: finalFilters }));
      console.log("action  de filter est appelee ");
    }
  }

  /**
   * ============================
   * RÉINITIALISER LES FILTRES
   * ============================
   */
  clearFilters(): void {
    this.store.dispatch(ListingsActions.clearFilters());
    this.store.dispatch(ListingsActions.loadAllProperties({ page: 0, size: 50 }));
    this.isSearchMode = false;
    this.router.navigate(['/listings']);
  }

  /**
   * ============================
   * NAVIGUER VERS DÉTAIL PROPERTY
   * ============================
   */
  viewPropertyDetail(propertyId: number): void {
    this.router.navigate(['/listings', propertyId]);
  }

  /**
   * ============================
   * TRACK BY POUR OPTIMISER LE RENDERING
   * ============================
   */
  trackByPropertyId(index: number, property: any): number {
    return property.propertyId;
  }

}
