// src/app/core/models/property-photo.model.ts
export interface PropertyPhoto {
  photoId: number;
  photoUrl: string;
  photoHash?: string;
  isCover: boolean;
  displayOrder: number;
}
