// src/app/features/host/property-wizard/steps/step-photos/step-photos.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

import { PhotoFile } from '../../property-wizard.component';

@Component({
  selector: 'app-step-photos',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    DragDropModule
  ],
  templateUrl: './step-photos.component.html',
  styleUrl: './step-photos.component.scss'
})
export class StepPhotosComponent {
  @Input() photos: PhotoFile[] = [];
  @Output() photosChange = new EventEmitter<PhotoFile[]>();

  private snackBar: MatSnackBar;

  constructor(snackBar: MatSnackBar) {
    this.snackBar = snackBar;
  }

  /**
   * Handle file selection
   */
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const files = Array.from(input.files);
    this.addFiles(files);

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
      this.addFiles(files);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  /**
   * Add files to the list
   */
  private addFiles(files: File[]): void {
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

    // Create PhotoFile objects
    validFiles.forEach((file, index) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const newPhoto: PhotoFile = {
          file: file,
          preview: e.target?.result as string,
          isCover: this.photos.length === 0 && index === 0, // First photo is cover
          displayOrder: this.photos.length + index + 1
        };
        this.photos.push(newPhoto);
        this.updateDisplayOrder();
        this.photosChange.emit(this.photos);
      };
      reader.readAsDataURL(file);
    });
  }

  /**
   * Remove a photo
   */
  removePhoto(index: number): void {
    const wascover = this.photos[index].isCover;
    this.photos.splice(index, 1);

    // If removed photo was cover, make first one cover
    if (wascover && this.photos.length > 0) {
      this.photos[0].isCover = true;
    }

    this.updateDisplayOrder();
    this.photosChange.emit(this.photos);
  }

  /**
   * Set photo as cover
   */
  setCover(index: number): void {
    this.photos.forEach((photo, i) => {
      photo.isCover = i === index;
    });
    this.photosChange.emit(this.photos);
  }

  /**
   * Handle reordering
   */
  drop(event: CdkDragDrop<PhotoFile[]>): void {
    moveItemInArray(this.photos, event.previousIndex, event.currentIndex);
    this.updateDisplayOrder();
    this.photosChange.emit(this.photos);
  }

  /**
   * Update display order after changes
   */
  private updateDisplayOrder(): void {
    this.photos.forEach((photo, index) => {
      photo.displayOrder = index + 1;
    });
  }
}
