// src/app/features/property-detail/photo-gallery-dialog/photo-gallery-dialog.component.ts

import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface PhotoGalleryData {
  photos: string[];
  startIndex: number;
  propertyTitle: string;
}

/**
 * ============================
 * PHOTO GALLERY DIALOG
 * Carousel fullscreen pour voir toutes les photos
 * ============================
 */
@Component({
  selector: 'app-photo-gallery-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './photo-gallery-dialog.component.html',
  styleUrl: './photo-gallery-dialog.component.scss'
})
export class PhotoGalleryDialogComponent implements OnInit {

  currentIndex: number = 0;
  photos: string[] = [];
  propertyTitle: string = '';

  constructor(
    public dialogRef: MatDialogRef<PhotoGalleryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PhotoGalleryData
  ) {
    this.photos = data.photos;
    this.currentIndex = data.startIndex || 0;
    this.propertyTitle = data.propertyTitle;
  }

  ngOnInit(): void {
    // Écouter les touches clavier
    document.addEventListener('keydown', this.handleKeyPress.bind(this));
  }

  ngOnDestroy(): void {
    document.removeEventListener('keydown', this.handleKeyPress.bind(this));
  }

  /**
   * ============================
   * NAVIGATION
   * ============================
   */
  previous(): void {
    if (this.currentIndex > 0) {
      this.currentIndex--;
    } else {
      this.currentIndex = this.photos.length - 1; // Boucle
    }
  }

  next(): void {
    if (this.currentIndex < this.photos.length - 1) {
      this.currentIndex++;
    } else {
      this.currentIndex = 0; // Boucle
    }
  }

  goToPhoto(index: number): void {
    this.currentIndex = index;
  }

  /**
   * ============================
   * GESTION CLAVIER
   * Flèche gauche/droite, Escape
   * ============================
   */
  handleKeyPress(event: KeyboardEvent): void {
    switch(event.key) {
      case 'ArrowLeft':
        this.previous();
        break;
      case 'ArrowRight':
        this.next();
        break;
      case 'Escape':
        this.close();
        break;
    }
  }

  /**
   * ============================
   * FERMER
   * ============================
   */
  close(): void {
    this.dialogRef.close();
  }

  /**
   * ============================
   * GETTERS
   * ============================
   */
  get currentPhoto(): string {
    return this.photos[this.currentIndex];
  }

  get photoCounter(): string {
    return `${this.currentIndex + 1} / ${this.photos.length}`;
  }

  get hasPrevious(): boolean {
    return this.currentIndex > 0;
  }

  get hasNext(): boolean {
    return this.currentIndex < this.photos.length - 1;
  }
}
