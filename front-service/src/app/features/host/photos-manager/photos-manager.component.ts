// src/app/features/host/host-property-detail/components/photos-manager/photos-manager.component.ts
import { Component, Input, OnInit, OnChanges, SimpleChanges, inject, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

import { MediaService, ImageUploadResponse } from '../../../core/services/media.service';
import { PropertyService } from '../../../core/services/property.service';
import { PropertyPhoto } from '../../../core/models/property-photo.model';


@Component({
  selector: 'app-photos-manager',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatMenuModule,
    DragDropModule
  ],
  templateUrl: './photos-manager.component.html',
  styleUrl: './photos-manager.component.scss'
})
export class PhotosManagerComponent implements OnInit, OnChanges {
  @Input() propertyId!: number;
  @Input() photos: PropertyPhoto[] = [];
  @Output() photosChanged = new EventEmitter<void>();

  private mediaService = inject(MediaService);
  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);

  // Sorted photos
  sortedPhotos: PropertyPhoto[] = [];

  // Upload state
  uploading = false;
  uploadProgress = 0;
  uploadQueue: File[] = [];
  currentUploadIndex = 0;

  // Other states
  deleting: number | null = null;
  settingCover: number | null = null;
  reordering = false;

  ngOnInit(): void {
    this.sortPhotos();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['photos']) {
      this.sortPhotos();
    }
  }

  /**
   * Sort photos by displayOrder
   */
  private sortPhotos(): void {
    this.sortedPhotos = [...this.photos].sort((a, b) =>
      (a.displayOrder || 0) - (b.displayOrder || 0)
    );
  }

  /**
   * Handle file selection
   */
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files = Array.from(input.files);
    this.validateAndQueueFiles(files);

    // Reset input
    input.value = '';
  }

  /**
   * Handle drag and drop files
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();

    if (event.dataTransfer?.files) {
      const files = Array.from(event.dataTransfer.files);
      this.validateAndQueueFiles(files);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  /**
   * Validate and queue files for upload
   */
  private validateAndQueueFiles(files: File[]): void {
    const validFiles = files.filter(file => {
      // Check type
      if (!file.type.startsWith('image/')) {
        this.snackBar.open(`${file.name} is not an image`, 'Close', { duration: 3000 });
        return false;
      }

      // Check size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        this.snackBar.open(`${file.name} is too large (max 10MB)`, 'Close', { duration: 3000 });
        return false;
      }

      return true;
    });

    if (validFiles.length > 0) {
      this.uploadQueue = validFiles;
      this.startUpload();
    }
  }

  /**
   * Start uploading files
   */
  private async startUpload(): Promise<void> {
    this.uploading = true;
    this.currentUploadIndex = 0;

    const startOrder = this.sortedPhotos.length + 1;

    for (let i = 0; i < this.uploadQueue.length; i++) {
      this.currentUploadIndex = i + 1;
      this.uploadProgress = (i / this.uploadQueue.length) * 100;

      const file = this.uploadQueue[i];
      const isCover = this.sortedPhotos.length === 0 && i === 0; // First photo is cover if no photos exist
      const displayOrder = startOrder + i;

      try {
        await this.mediaService.uploadPropertyImage(
          file,
          this.propertyId,
          isCover,
          displayOrder
        ).toPromise();
      } catch (error: any) {
        console.error('Upload error:', error);
        this.snackBar.open(`Failed to upload ${file.name}`, 'Close', { duration: 3000 });
      }
    }

    this.uploading = false;
    this.uploadProgress = 100;
    this.uploadQueue = [];

    this.snackBar.open(`${this.currentUploadIndex} photo(s) uploaded successfully!`, 'Close', { duration: 3000 });
    this.photosChanged.emit();
  }

  /**
   * Delete a photo
   */
  deletePhoto(photo: PropertyPhoto): void {
    if (!confirm('Are you sure you want to delete this photo?')) {
      return;
    }

    this.deleting = photo.photoId;

    this.mediaService.deletePropertyPhoto(photo.photoId).subscribe({
      next: () => {
        this.deleting = null;
        this.snackBar.open('Photo deleted successfully!', 'Close', { duration: 2000 });
        this.photosChanged.emit();
      },
      error: (error) => {
        this.deleting = null;
        console.error('Delete error:', error);
        this.snackBar.open('Failed to delete photo', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Set photo as cover
   */
  setCover(photo: PropertyPhoto): void {
    if (photo.isCover) return;

    this.settingCover = photo.photoId;

    this.propertyService.setPhotoCover(this.propertyId, photo.photoId).subscribe({
      next: () => {
        this.settingCover = null;
        this.snackBar.open('Cover photo updated!', 'Close', { duration: 2000 });
        this.photosChanged.emit();
      },
      error: (error) => {
        this.settingCover = null;
        console.error('Set cover error:', error);
        this.snackBar.open('Failed to set cover photo', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Handle photo reordering (drag & drop)
   */
  onPhotoDrop(event: CdkDragDrop<PropertyPhoto[]>): void {
    if (event.previousIndex === event.currentIndex) return;

    moveItemInArray(this.sortedPhotos, event.previousIndex, event.currentIndex);
    this.saveNewOrder();
  }

  /**
   * Save new photo order
   */
  private saveNewOrder(): void {
    this.reordering = true;

    const photoIds = this.sortedPhotos.map(p => p.photoId);

    this.propertyService.reorderPhotos(this.propertyId, photoIds).subscribe({
      next: () => {
        this.reordering = false;
        this.snackBar.open('Photo order saved!', 'Close', { duration: 2000 });
        // Update local displayOrder
        this.sortedPhotos.forEach((photo, index) => {
          photo.displayOrder = index + 1;
        });
      },
      error: (error) => {
        this.reordering = false;
        console.error('Reorder error:', error);
        this.snackBar.open('Failed to save photo order', 'Close', { duration: 3000 });
        // Revert to original order
        this.sortPhotos();
      }
    });
  }

  /**
   * Get photo URL (handle both full URL and relative path)
   */
  getPhotoUrl(photo: PropertyPhoto): string {
    if (!photo.photoUrl) return '';

    // If it's already a full URL, return as is
    if (photo.photoUrl.startsWith('http')) {
      return photo.photoUrl;
    }

    // Otherwise, build the URL (for local development)
    return `http://localhost:8087/api/media/files/${photo.photoUrl}`;
  }
}
