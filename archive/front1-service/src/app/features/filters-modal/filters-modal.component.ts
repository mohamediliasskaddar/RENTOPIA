// src/app/features/listings/components/filters-modal/filters-modal.component.ts

import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';

// Material Imports
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSliderModule } from '@angular/material/slider';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

// Models
import { PropertySearchFilters } from './../../core/models/property.model';
import { Amenity } from './../../core/models/amenity.model';

// Services
import { AmenityService } from './../../core/services/amenity.service';

/**
 * ============================
 * COMPOSANT FILTERS MODAL
 * Modal de filtres avancés (style Airbnb)
 * ============================
 */
@Component({
  selector: 'app-filters-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatSliderModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './filters-modal.component.html',
  styleUrl: './filters-modal.component.scss'
})
export class FiltersModalComponent implements OnInit {

  filtersForm!: FormGroup;
  amenities: Amenity[] = [];
  selectedAmenities: number[] = [];

  // Affichage des property types
  showAllPropertyTypes = false;

  // Affichage des amenities
  showAllAmenities = false;
  amenitiesDisplayLimit = 8; // Nombre d'amenities à afficher par défaut

  // Property types de base (affichés par défaut)
  basicPropertyTypes = [
    { value: 'house', label: 'House', icon: 'home' },
    { value: 'apartment', label: 'Apartment', icon: 'apartment' },
    { value: 'villa', label: 'Villa', icon: 'villa' },
    { value: 'studio', label: 'Studio', icon: 'meeting_room' },
    { value: 'loft', label: 'Loft', icon: 'layers' },
    { value: 'private_room', label: 'Private Room', icon: 'room' }
  ];

  // Property types additionnels (affichés seulement si "Show more")
  additionalPropertyTypes = [
    { value: 'entire_place', label: 'Entire Place', icon: 'holiday_village' },
    { value: 'shared_room', label: 'Shared Room', icon: 'bed' },
    { value: 'bungalow', label: 'Bungalow', icon: 'cottage' },
    { value: 'townhouse', label: 'Townhouse', icon: 'other_houses' },
    { value: 'penthouse', label: 'Penthouse', icon: 'pentagon' },
    { value: 'duplex', label: 'Duplex', icon: 'house_siding' },
    { value: 'guest_house', label: 'Guest House', icon: 'houseboat' },
    { value: 'hotel_room', label: 'Hotel Room', icon: 'king_bed' },
    { value: 'cabin', label: 'Cabin', icon: 'cabin' },
    { value: 'chalet', label: 'Chalet', icon: 'house' },
    { value: 'farm', label: 'Farm', icon: 'agriculture' },
    { value: 'riad', label: 'Riad', icon: 'mosque' },
    { value: 'residence', label: 'Residence', icon: 'domain' },
    { value: 'condo', label: 'Condominium', icon: 'location_city' },
    { value: 'room_in_riad', label: 'Room in Riad', icon: 'bedroom_child' },
    { value: 'guest_suite', label: 'Guest Suite', icon: 'bedroom_parent' }
  ];




  // Tous les types (pour la recherche)
  get allPropertyTypes() {
    return [...this.basicPropertyTypes, ...this.additionalPropertyTypes];
  }

  // Types affichés actuellement
  get displayedPropertyTypes() {
    return this.showAllPropertyTypes ? this.allPropertyTypes : this.basicPropertyTypes;
  }

  // Amenities affichées actuellement
  get displayedAmenities() {
    return this.showAllAmenities ? this.amenities : this.amenities.slice(0, this.amenitiesDisplayLimit);
  }

  // Vérifier s'il y a plus d'amenities à afficher
  get hasMoreAmenities() {
    return this.amenities.length > this.amenitiesDisplayLimit;
  }

  // Prix min/max
  minPrice = 0;
  maxPrice = 1000;
  priceRange = [0, 1000];

  constructor(
    public dialogRef: MatDialogRef<FiltersModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private amenityService: AmenityService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadAmenities();

    // Pré-remplir avec les filtres existants
    if (this.data?.currentFilters) {
      this.prefillFilters(this.data.currentFilters);
    }
  }

  /**
   * ============================
   * INITIALISER LE FORMULAIRE
   * ============================
   */
  private initForm(): void {
    this.filtersForm = this.fb.group({
      propertyType: [null],
      placeType: [null],
      minPrice: [this.minPrice],
      maxPrice: [this.maxPrice],
      bedrooms: [null],
      bathrooms: [null],
      beds: [null],
      instantBooking: [false],
      smokingAllowed: [false],
      eventsAllowed: [false]
    });
  }

  /**
   * ============================
   * CHARGER LES AMENITIES
   * ============================
   */
  private loadAmenities(): void {
    this.amenityService.getAllAmenities().subscribe({
      next: (amenities) => {
        this.amenities = amenities;
      },
      error: (error) => {
        console.error('Erreur chargement amenities:', error);
      }
    });
  }

  /**
   * ============================
   * PRÉ-REMPLIR LES FILTRES
   * ============================
   */
  private prefillFilters(filters: PropertySearchFilters): void {
    if (filters.propertyType) {
      this.filtersForm.patchValue({ propertyType: filters.propertyType });
    }
    if (filters.minPrice !== undefined) {
      this.priceRange[0] = filters.minPrice;
      this.filtersForm.patchValue({ minPrice: filters.minPrice });
    }
    if (filters.maxPrice !== undefined) {
      this.priceRange[1] = filters.maxPrice;
      this.filtersForm.patchValue({ maxPrice: filters.maxPrice });
    }
    if (filters.bedrooms) {
      this.filtersForm.patchValue({ bedrooms: filters.bedrooms });
    }
    if (filters.amenityIds) {
      this.selectedAmenities = [...filters.amenityIds];
    }
  }

  /**
   * ============================
   * SÉLECTIONNER UN TYPE DE PROPERTY
   * ============================
   */
  selectPropertyType(type: string): void {
    const currentType = this.filtersForm.value.propertyType;
    this.filtersForm.patchValue({
      propertyType: currentType === type ? null : type
    });
  }

  /**
   * ============================
   * TOGGLE AMENITY
   * ============================
   */
  toggleAmenity(amenityId: number): void {
    const index = this.selectedAmenities.indexOf(amenityId);
    if (index > -1) {
      this.selectedAmenities.splice(index, 1);
    } else {
      this.selectedAmenities.push(amenityId);
    }
  }

  /**
   * ============================
   * VÉRIFIER SI AMENITY EST SÉLECTIONNÉE
   * ============================
   */
  isAmenitySelected(amenityId: number): boolean {
    return this.selectedAmenities.includes(amenityId);
  }

  /**
   * ============================
   * MAJ PRIX MIN/MAX
   * ============================
   */
  onPriceRangeChange(event: any): void {
    this.priceRange = [event.value[0], event.value[1]];
    this.filtersForm.patchValue({
      minPrice: event.value[0],
      maxPrice: event.value[1]
    });
  }

  /**
   * ============================
   * INCRÉMENTER/DÉCRÉMENTER
   * ============================
   */
  increment(field: string): void {
    const current = this.filtersForm.value[field] || 0;
    this.filtersForm.patchValue({ [field]: current + 1 });
  }

  decrement(field: string): void {
    const current = this.filtersForm.value[field] || 0;
    if (current > 0) {
      this.filtersForm.patchValue({ [field]: current - 1 });
    }
  }

  /**
   * ============================
   * TOGGLE AFFICHAGE DES TYPES ADDITIONNELS
   * ============================
   */
  togglePropertyTypes(): void {
    this.showAllPropertyTypes = !this.showAllPropertyTypes;
  }

  /**
   * ============================
   * TOGGLE AFFICHAGE DES AMENITIES
   * ============================
   */
  toggleAmenities(): void {
    this.showAllAmenities = !this.showAllAmenities;
  }

  /**
   * ============================
   * RÉINITIALISER LES FILTRES
   * ============================
   */
  clearAll(): void {
    this.filtersForm.reset({
      minPrice: this.minPrice,
      maxPrice: this.maxPrice,
      instantBooking: false,
      smokingAllowed: false,
      eventsAllowed: false
    });
    this.selectedAmenities = [];
    this.priceRange = [this.minPrice, this.maxPrice];
  }

  /**
   * ============================
   * APPLIQUER LES FILTRES
   * ✅ CORRECTION : Ne pas envoyer false/0/null, envoyer undefined
   * ============================
   */
  applyFilters(): void {
    const formValues = this.filtersForm.value;

    const filters: PropertySearchFilters = {
      adults: 1, // Valeur par défaut
      propertyType: formValues.propertyType || undefined,
      placeType: formValues.placeType || undefined,
      minPrice: formValues.minPrice > 0 ? formValues.minPrice : undefined,
      maxPrice: formValues.maxPrice < this.maxPrice ? formValues.maxPrice : undefined,
      bedrooms: formValues.bedrooms > 0 ? formValues.bedrooms : undefined,
      bathrooms: formValues.bathrooms > 0 ? formValues.bathrooms : undefined,
      beds: formValues.beds > 0 ? formValues.beds : undefined,

      // ✅ CORRECTION : Seulement si true, sinon undefined
      instantBooking: formValues.instantBooking === true ? true : undefined,
      smokingAllowed: formValues.smokingAllowed === true ? true : undefined,
      eventsAllowed: formValues.eventsAllowed === true ? true : undefined,

      amenityIds: this.selectedAmenities.length > 0 ? this.selectedAmenities : undefined
    };

    // Supprimer les valeurs undefined pour nettoyer l'objet
    Object.keys(filters).forEach(key => {
      if (filters[key as keyof PropertySearchFilters] === undefined) {
        delete filters[key as keyof PropertySearchFilters];
      }
    });

    console.log('✅ Filtres nettoyés à envoyer:', filters);

    this.dialogRef.close(filters);
  }

  /**
   * ============================
   * FERMER SANS APPLIQUER
   * ============================
   */
  close(): void {
    this.dialogRef.close();
  }
}
