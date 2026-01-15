// src/app/core/services/property-version.service.ts

import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {environment} from "../../../environments/environment";

/**
 * Service pour récupérer les versions de property avec leurs snapshots
 */
@Injectable({
  providedIn: 'root'
})
export class PropertyVersionService {
  private readonly baseUrl = environment.services.listing; // '/listings'
  private apiService = inject(ApiService);

  /**
   * Récupérer une version spécifique par ID
   */
  getVersionById(versionId: number): Observable<PropertyVersionResponse> {
    return this.apiService.get<PropertyVersionResponse>(`${this.baseUrl}/property-versions/${versionId}`);
  }

  /**
   * Récupérer la version actuelle d'une property
   */
  getCurrentVersion(propertyId: number): Observable<PropertyVersionResponse> {
    return this.apiService.get<PropertyVersionResponse>(
      `${this.baseUrl}/property-versions/property/${propertyId}/current`
    );
  }

  /**
   * Récupérer toutes les versions d'une property
   */
  getAllVersionsByProperty(propertyId: number): Observable<PropertyVersionResponse[]> {
    return this.apiService.get<PropertyVersionResponse[]>(
      `${this.baseUrl}/property-versions/property/${propertyId}`
    );
  }
}

/**
 * Response de PropertyVersion depuis le backend
 */
export interface PropertyVersionResponse {
  versionId: number;
  propertyId: number;
  numVersion: number;
  createdAt: string;

  generalSnapshot: GeneralSnapshot;
  amenitiesSnapshot: AmenitiesSnapshot;
  photosSnapshot: PhotosSnapshot;
  rulesSnapshot: RulesSnapshot;
}

export interface GeneralSnapshot {
  snapshotId: number;
  snapshotHash: string;
  generalJson: string;
  createdAt: string;
}

export interface AmenitiesSnapshot {
  snapshotId: number;
  snapshotHash: string;
  amenitiesJson: string;
  createdAt: string;
}

export interface PhotosSnapshot {
  snapshotId: number;
  snapshotHash: string;
  photosJson: string;
  createdAt: string;
}

export interface RulesSnapshot {
  snapshotId: number;
  snapshotHash: string;
  childrenAllowed: boolean;
  babiesAllowed: boolean;
  petsAllowed: boolean;
  smokingAllowed: boolean;
  eventsAllowed: boolean;
  customRules: string;
  createdAt: string;
}
