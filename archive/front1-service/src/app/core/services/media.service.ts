// src/app/core/services/media.service.ts
import { Injectable, inject } from '@angular/core';
import { Observable, from } from 'rxjs';
import { ApiService } from './api.service';

export interface ImageUploadResponse {
  photoId: number;
  s3Key: string;
  cdnUrl: string;
  thumbnailUrl?: string;
  fileSize: number;
  contentType: string;
  width?: number;
  height?: number;
  isCover: boolean;
  displayOrder: number;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private baseUrl = '/media';
  private apiService = inject(ApiService);

  /**
   * Upload une image de property
   */
  uploadPropertyImage(
    file: File,
    propertyId: number,
    isCover: boolean = false,
    displayOrder: number = 0
  ): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('entityId', propertyId.toString());
    formData.append('isCover', isCover.toString());
    formData.append('displayOrder', displayOrder.toString());

    return this.apiService.upload<ImageUploadResponse>(
      `${this.baseUrl}/properties/upload`,
      formData
    );
  }

  /**
   * Upload une photo de profil
   */
  uploadUserPhoto(file: File, userId: number): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId.toString());

    return this.apiService.upload<ImageUploadResponse>(
      `${this.baseUrl}/users/upload`,
      formData
    );
  }

  /**
   * Supprimer une photo de property
   */
  deletePropertyPhoto(photoId: number): Observable<void> {
    return this.apiService.delete<void>(`${this.baseUrl}/properties/${photoId}`);
  }
}
