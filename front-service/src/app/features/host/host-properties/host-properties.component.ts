// src/app/features/host/host-properties/host-properties.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import * as ListingsActions from '../../../store/listings/listing.actions';
import {
  selectMyProperties,
  selectMyPropertiesLoaded,
  selectListingsLoading,
  selectListingsError
} from '../../../store/listings/listing.selectors';
import { selectCurrentUser } from '../../../store/auth/auth.selectors';
import { PropertyCard } from '../../../core/models/property-card.model';
import {EthPricePipe} from "../../../core/pipes/eth-price.pipe"; // ✅ Changer ici

@Component({
  selector: 'app-host-properties',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule,
    EthPricePipe
  ],
  templateUrl: './host-properties.component.html',
  styleUrl: './host-properties.component.scss'
})
export class HostPropertiesComponent implements OnInit {
  private store = inject(Store);
  private snackBar = inject(MatSnackBar);

  // Selectors
  currentUser$ = this.store.select(selectCurrentUser);
  myProperties$ = this.store.select(selectMyProperties);
  myPropertiesLoaded$ = this.store.select(selectMyPropertiesLoaded);
  loading$ = this.store.select(selectListingsLoading);
  error$ = this.store.select(selectListingsError);

  // Filter
  selectedStatus: string | null = null;

  ngOnInit(): void {
    this.currentUser$.subscribe(user => {
      if (user?.id) {
        this.store.dispatch(ListingsActions.loadMyProperties({ userId: user.id }));
      }
    });
  }

  /**
   * Get status chip color
   */
  getStatusColor(status: string | undefined): string {
    if (!status) return '';
    const colors: Record<string, string> = {
      'ACTIVE': 'primary',
      'DRAFT': 'warn',
      'ARCHIVED': 'accent',
      'DELETED': ''
    };
    return colors[status] || '';
  }

  /**
   * Get status label
   */
  getStatusLabel(status: string | undefined | null): string {
    if (!status) return '';
    const labels: Record<string, string> = {
      'ACTIVE': 'Active',
      'DRAFT': 'Draft',
      'ARCHIVED': 'Archived',
      'DELETED': 'Deleted'
    };
    return labels[status] || status;
  }

  /**
   * Publish a draft property
   */
  publishProperty(propertyId: number, event: Event): void {
    event.stopPropagation();
    this.store.dispatch(ListingsActions.publishProperty({ propertyId }));
    this.snackBar.open('Property published successfully!', 'Close', {
      duration: 3000
    });
  }

  /**
   * Delete a property
   */
  deleteProperty(propertyId: number, event: Event): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this property?')) {
      this.store.dispatch(ListingsActions.deleteProperty({ propertyId }));
      this.snackBar.open('Property deleted', 'Close', {
        duration: 3000
      });
    }
  }

  /**
   * Filter properties by status
   */
  filterByStatus(status: string | null): void {
    this.selectedStatus = status;
  }

  /**
   * Get filtered properties - ✅ Utiliser PropertyCard[]
   */
  getFilteredProperties(properties: PropertyCard[]): PropertyCard[] {
    if (!this.selectedStatus) {
      return properties;
    }
    return properties.filter(p => p.status === this.selectedStatus);
  }

  /**
   * Get main photo URL - ✅ Utiliser PropertyCard
   */
  getMainPhoto(property: PropertyCard): string {
    return property.mainPhotoUrl || 'assets/images/logo.png';
  }

  /**
   * Track by function - ✅ Utiliser PropertyCard
   */
  trackByPropertyId(index: number, property: PropertyCard): number {
    return property.propertyId;
  }
}
