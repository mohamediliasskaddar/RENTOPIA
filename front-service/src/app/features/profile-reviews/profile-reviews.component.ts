// src/app/features/profile/components/profile-reviews/profile-reviews.component.ts
// ✅ VERSION AVEC DEBUG IMAGES

import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { Store } from '@ngrx/store';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';

// Material
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';

// Store
import {
  selectUserReviews,
  selectProfileLoading,
  selectVisibleReviews,
  selectAverageRating
} from '../../store/profile/profile.selectors';

// Models
import { ReviewWithProperty } from '../../core/models/review.model';

@Component({
  selector: 'app-profile-reviews',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  templateUrl: './profile-reviews.component.html',
  styleUrl: './profile-reviews.component.scss'
})
export class ProfileReviewsComponent implements OnInit, OnDestroy {

  private store = inject(Store);
  private router = inject(Router);
  private sanitizer = inject(DomSanitizer);
  private destroy$ = new Subject<void>();

  // Observables
  reviews$ = this.store.select(selectUserReviews);
  loading$ = this.store.select(selectProfileLoading);
  visibleReviews$ = this.store.select(selectVisibleReviews);
  averageRating$ = this.store.select(selectAverageRating);

  ngOnInit(): void {
    console.log('🎬 ProfileReviewsComponent - Init');

    // Debug: Observer les reviews
    this.reviews$.pipe(takeUntil(this.destroy$)).subscribe(reviews => {
      console.log('⭐ Reviews:', reviews);

      // Debug chaque photo
      reviews.forEach((review, index) => {
        console.log(`📸 Review #${index + 1}:`, {
          propertyId: review.propertyId,
          propertyTitle: review.propertyTitle,
          photoUrl: review.propertyMainPhoto,
          photoType: typeof review.propertyMainPhoto,
          photoLength: review.propertyMainPhoto?.length
        });
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * ✅ NOUVEAU: Sanitize l'URL pour background-image
   */
  sanitizeImageUrl(url: string | null): SafeStyle {
    if (!url) {
      return this.sanitizer.bypassSecurityTrustStyle('none');
    }

    // Debug
    console.log('🖼️ Sanitizing URL:', url);

    // Si l'URL contient déjà 'url()', la nettoyer
    const cleanUrl = url.replace(/^url\(['"]?/, '').replace(/['"]?\)$/, '');

    // Créer le style background-image
    const style = `url('${cleanUrl}')`;
    console.log('✅ Sanitized style:', style);

    return this.sanitizer.bypassSecurityTrustStyle(style);
  }

  /**
   * ✅ NOUVEAU: Gérer les erreurs d'image
   */
  onImageError(event: Event): void {
    console.error('❌ Image failed to load:', event);
    const imgElement = event.target as HTMLImageElement;
    console.error('Failed URL:', imgElement.src);

    // Optionnel: Remplacer par une image placeholder
    // imgElement.src = 'assets/images/placeholder.jpg';
  }

  /**
   * Générer tableau d'étoiles pour affichage
   */
  getStars(rating: number): number[] {
    const fullStars = Math.floor(rating);
    return Array(5).fill(0).map((_, i) => i < fullStars ? 1 : 0);
  }

  /**
   * Formater la date
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  /**
   * Naviguer vers la propriété
   */
  onViewProperty(propertyId: number): void {
    console.log('🏠 Navigate to property:', propertyId);
    this.router.navigate(['/property', propertyId]);
  }
}
