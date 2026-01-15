// src/app/features/host/host-property-detail/components/amenities-manager/amenities-manager.component.ts
import { Component, Input, OnInit, OnChanges, SimpleChanges, inject, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatBadgeModule } from '@angular/material/badge';

import { AmenityService } from '../../../core/services/amenity.service';
import { PropertyService } from '../../../core/services/property.service';
import { Amenity } from '../../../core/models/amenity.model';
import { PropertyAmenity } from '../../../core/models/property-detail.model';

@Component({
  selector: 'app-amenities-manager',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatExpansionModule,
    MatBadgeModule
  ],
  templateUrl: './amenities-manager.component.html',
  styleUrl: './amenities-manager.component.scss'
})
export class AmenitiesManagerComponent implements OnInit, OnChanges {
  @Input() propertyId!: number;
  @Input() currentAmenities: PropertyAmenity[] = [];
  @Output() amenitiesChanged = new EventEmitter<void>();

  private amenityService = inject(AmenityService);
  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);

  // All available amenities
  allAmenities: Amenity[] = [];
  amenitiesByCategory: Map<string, Amenity[]> = new Map();

  // Current property amenity IDs (for quick lookup)
  currentAmenityIds: Set<number> = new Set();

  // Loading states
  loading = false;
  saving = false;

  // Edit mode
  editMode = false;

  ngOnInit(): void {
    this.loadAllAmenities();
    this.updateCurrentAmenityIds();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentAmenities']) {
      this.updateCurrentAmenityIds();
    }
  }

  /**
   * Load all available amenities
   */
  loadAllAmenities(): void {
    this.loading = true;

    this.amenityService.getAllAmenities().subscribe({
      next: (amenities) => {
        this.allAmenities = amenities;
        this.groupAmenitiesByCategory();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading amenities:', error);
        this.loading = false;
        this.snackBar.open('Error loading amenities', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Group amenities by category
   */
  groupAmenitiesByCategory(): void {
    this.amenitiesByCategory.clear();

    this.allAmenities.forEach(amenity => {
      const category = amenity.category || 'Other';
      if (!this.amenitiesByCategory.has(category)) {
        this.amenitiesByCategory.set(category, []);
      }
      this.amenitiesByCategory.get(category)!.push(amenity);
    });
  }

  /**
   * Update the set of current amenity IDs
   */
  updateCurrentAmenityIds(): void {
    this.currentAmenityIds.clear();
    this.currentAmenities.forEach(a => {
      this.currentAmenityIds.add(a.amenityId);
    });
  }

  /**
   * Check if amenity is selected
   */
  isSelected(amenityId: number): boolean {
    return this.currentAmenityIds.has(amenityId);
  }

  /**
   * Toggle amenity selection
   */
  toggleAmenity(amenity: Amenity): void {
    if (this.saving) return;

    const isCurrentlySelected = this.isSelected(amenity.amenityId);

    this.saving = true;

    if (isCurrentlySelected) {
      // Remove amenity
      this.propertyService.removeAmenityFromProperty(this.propertyId, amenity.amenityId).subscribe({
        next: () => {
          this.currentAmenityIds.delete(amenity.amenityId);
          this.saving = false;
          this.snackBar.open(`${amenity.name} removed`, 'Close', { duration: 2000 });
          this.amenitiesChanged.emit();
        },
        error: (error) => {
          this.saving = false;
          this.snackBar.open(error.message || 'Error removing amenity', 'Close', { duration: 3000 });
        }
      });
    } else {
      // Add amenity
      this.propertyService.addAmenityToProperty(this.propertyId, amenity.amenityId).subscribe({
        next: () => {
          this.currentAmenityIds.add(amenity.amenityId);
          this.saving = false;
          this.snackBar.open(`${amenity.name} added`, 'Close', { duration: 2000 });
          this.amenitiesChanged.emit();
        },
        error: (error) => {
          this.saving = false;
          this.snackBar.open(error.message || 'Error adding amenity', 'Close', { duration: 3000 });
        }
      });
    }
  }

  /**
   * Get count of selected amenities in a category
   */
  getSelectedCountInCategory(category: string): number {
    const amenitiesInCategory = this.amenitiesByCategory.get(category) || [];
    return amenitiesInCategory.filter(a => this.isSelected(a.amenityId)).length;
  }

  /**
   * Toggle edit mode
   */
  toggleEditMode(): void {
    this.editMode = !this.editMode;
  }

  /**
   * Get current amenities grouped by category
   */
  getCurrentAmenitiesByCategory(): Map<string, PropertyAmenity[]> {
    const map = new Map<string, PropertyAmenity[]>();

    this.currentAmenities.forEach(amenity => {
      const category = amenity.category || 'Other';
      if (!map.has(category)) {
        map.set(category, []);
      }
      map.get(category)!.push(amenity);
    });

    return map;
  }
}
