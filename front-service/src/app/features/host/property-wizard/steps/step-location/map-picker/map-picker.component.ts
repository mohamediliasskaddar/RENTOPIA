// src/app/features/host/property-wizard/steps/step-location/map-picker/map-picker.component.ts
import { Component, OnInit, AfterViewInit, Output, EventEmitter, Input, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import * as L from 'leaflet';

// Fix for default marker icons
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

@Component({
  selector: 'app-map-picker',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <div class="map-picker">
      <div class="map-header">
        <h4>Click on the map to select location</h4>
        <button mat-stroked-button (click)="centerOnCurrentLocation()">
          <mat-icon>my_location</mat-icon>
          My Location
        </button>
      </div>
      <div id="map" class="map-container"></div>
      <div class="map-footer">
        @if (selectedLat && selectedLng) {
          <span class="coordinates">
            <mat-icon>place</mat-icon>
            {{ selectedLat.toFixed(6) }}, {{ selectedLng.toFixed(6) }}
          </span>
        }
        <div class="actions">
          <button mat-button (click)="onCancel()">Cancel</button>
          <button
            mat-raised-button
            color="primary"
            (click)="onConfirm()"
            [disabled]="!selectedLat || !selectedLng"
          >
            Confirm Location
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .map-picker {
      border: 1px solid #e0e0e0;
      border-radius: 12px;
      overflow: hidden;
      background: white;
    }

    .map-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f5f5f5;
      border-bottom: 1px solid #e0e0e0;

      h4 {
        margin: 0;
        font-size: 14px;
        font-weight: 500;
      }

      button {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .map-container {
      height: 350px;
      width: 100%;
    }

    .map-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #fafafa;
      border-top: 1px solid #e0e0e0;

      .coordinates {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #666;

        mat-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
          color: #1976d2;
        }
      }

      .actions {
        display: flex;
        gap: 8px;
      }
    }
  `]
})
export class MapPickerComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() initialLat: number = 33.5731;
  @Input() initialLng: number = -7.5898;
  @Output() locationSelected = new EventEmitter<{ lat: number; lng: number }>();
  @Output() cancelled = new EventEmitter<void>();

  private map!: L.Map;
  private marker: L.Marker | null = null;

  selectedLat: number | null = null;
  selectedLng: number | null = null;

  ngOnInit(): void {
    // Use initial values if provided
    if (this.initialLat && this.initialLng) {
      this.selectedLat = this.initialLat;
      this.selectedLng = this.initialLng;
    }
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap(): void {
    // Initialize map
    this.map = L.map('map').setView([this.initialLat, this.initialLng], 13);

    // Add tile layer (OpenStreetMap)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Add initial marker if coordinates exist
    if (this.selectedLat && this.selectedLng) {
      this.addMarker(this.selectedLat, this.selectedLng);
    }

    // Handle click on map
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.selectedLat = e.latlng.lat;
      this.selectedLng = e.latlng.lng;
      this.addMarker(e.latlng.lat, e.latlng.lng);
    });
  }

  private addMarker(lat: number, lng: number): void {
    // Remove existing marker
    if (this.marker) {
      this.map.removeLayer(this.marker);
    }

    // Add new marker
    this.marker = L.marker([lat, lng], { draggable: true }).addTo(this.map);

    // Handle marker drag
    this.marker.on('dragend', () => {
      const position = this.marker!.getLatLng();
      this.selectedLat = position.lat;
      this.selectedLng = position.lng;
    });
  }

  centerOnCurrentLocation(): void {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;

        this.selectedLat = lat;
        this.selectedLng = lng;

        this.map.setView([lat, lng], 15);
        this.addMarker(lat, lng);
      },
      (error) => {
        console.error('Geolocation error:', error);
        if (error.code === 1) {
          alert('Location permission denied. Please enable it in your browser settings:\n\n1. Click the lock icon 🔒 in the address bar\n2. Find "Location" and set it to "Allow"\n3. Reload the page');
        } else {
          alert('Unable to get your location. Please select manually on the map.');
        }
      }
    );
  }

  onConfirm(): void {
    if (this.selectedLat && this.selectedLng) {
      this.locationSelected.emit({
        lat: this.selectedLat,
        lng: this.selectedLng
      });
    }
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
