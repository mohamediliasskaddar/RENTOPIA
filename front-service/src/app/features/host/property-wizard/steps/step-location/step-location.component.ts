// src/app/features/host/property-wizard/steps/step-location/step-location.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { MapPickerComponent } from './map-picker/map-picker.component';

@Component({
  selector: 'app-step-location',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    MapPickerComponent
  ],
  templateUrl: './step-location.component.html',
  styleUrl: './step-location.component.scss'
})
export class StepLocationComponent {
  @Input() form!: FormGroup;

  showMapPicker = false;

  constructor(private snackBar: MatSnackBar) {}

  // Common countries
  countries = [
    'Morocco', 'France', 'Spain', 'United States', 'United Kingdom',
    'Germany', 'Italy', 'Portugal', 'Netherlands', 'Belgium',
    'Canada', 'Australia', 'Japan', 'United Arab Emirates', 'Switzerland'
  ];

  /**
   * Get current location from browser
   */
  getCurrentLocation(): void {
    if (!navigator.geolocation) {
      this.snackBar.open('Geolocation is not supported by your browser', 'Close', { duration: 3000 });
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.form.patchValue({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        });
        this.snackBar.open('Location detected!', 'Close', { duration: 2000 });
      },
      (error) => {
        console.error('Geolocation error:', error);
        if (error.code === 1) {
          this.snackBar.open('Location blocked. Click the 🔒 icon in address bar to allow.', 'Close', { duration: 5000 });
        } else {
          this.snackBar.open('Unable to get your location', 'Close', { duration: 3000 });
        }
      }
    );
  }

  /**
   * Open map picker
   */
  openMapPicker(): void {
    this.showMapPicker = true;
  }

  /**
   * Handle location selected from map
   */
  onLocationSelected(location: { lat: number; lng: number }): void {
    this.form.patchValue({
      latitude: location.lat,
      longitude: location.lng
    });
    this.showMapPicker = false;
    this.snackBar.open('Location selected!', 'Close', { duration: 2000 });
  }

  /**
   * Cancel map picker
   */
  onMapCancelled(): void {
    this.showMapPicker = false;
  }

  /**
   * Get initial latitude for map
   */
  getInitialLat(): number {
    return this.form.get('latitude')?.value || 33.5731;
  }

  /**
   * Get initial longitude for map
   */
  getInitialLng(): number {
    return this.form.get('longitude')?.value || -7.5898;
  }
}
