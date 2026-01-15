// src/app/features/host/property-wizard/steps/step-summary/step-summary.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

import { PhotoFile } from '../../property-wizard.component';

@Component({
  selector: 'app-step-summary',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatDividerModule
  ],
  templateUrl: './step-summary.component.html',
  styleUrl: './step-summary.component.scss'
})
export class StepSummaryComponent {
  @Input() propertyData: any;
  @Input() photos: PhotoFile[] = [];

  getPropertyTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      'APARTMENT': 'Apartment',
      'HOUSE': 'House',
      'VILLA': 'Villa',
      'STUDIO': 'Studio',
      'LOFT': 'Loft'
    };
    return labels[type] || type;
  }

  getPlaceTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      'ENTIRE_PLACE': 'Entire place',
      'PRIVATE_ROOM': 'Private room',
      'SHARED_ROOM': 'Shared room'
    };
    return labels[type] || type;
  }

  getCoverPhoto(): string | null {
    const cover = this.photos.find(p => p.isCover);
    return cover?.preview || this.photos[0]?.preview || null;
  }
}
