// src/app/features/reviews/components/rating-stars/rating-stars.component.ts

import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

/**
 * Composant pour afficher les étoiles de notation
 * Affiche des étoiles pleines, demi-étoiles, et étoiles vides
 */
@Component({
  selector: 'app-rating-stars',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="rating-stars" [class.large]="size === 'large'" [class.small]="size === 'small'">
      <span class="stars-container">
        <mat-icon
          *ngFor="let star of stars"
          [class.filled]="star === 'full'"
          [class.half]="star === 'half'"
          [class.empty]="star === 'empty'">
          {{ star === 'full' ? 'star' : star === 'half' ? 'star_half' : 'star_outline' }}
        </mat-icon>
      </span>
      <span class="rating-value" *ngIf="showValue">{{ rating?.toFixed(1) }}</span>
      <span class="rating-count" *ngIf="showCount && count">({{ count }})</span>
    </div>
  `,
  styles: [`
    .rating-stars {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .stars-container {
      display: flex;
      align-items: center;
      gap: 2px;
    }

    mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: #ddd;
      transition: color 0.2s ease;

      &.filled {
        color: #FFB400;
      }

      &.half {
        color: #FFB400;
      }

      &.empty {
        color: #ddd;
      }
    }

    .rating-stars.large mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .rating-stars.small mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .rating-value {
      font-size: 1rem;
      font-weight: 600;
      color: #222;
    }

    .rating-count {
      font-size: 0.9rem;
      color: #717171;
    }
  `]
})
export class RatingStarsComponent {
  @Input() rating: number = 0;
  @Input() count?: number;
  @Input() showValue: boolean = true;
  @Input() showCount: boolean = false;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';

  get stars(): ('full' | 'half' | 'empty')[] {
    const stars: ('full' | 'half' | 'empty')[] = [];
    const fullStars = Math.floor(this.rating);
    const hasHalfStar = this.rating % 1 >= 0.5;

    // Étoiles pleines
    for (let i = 0; i < fullStars; i++) {
      stars.push('full');
    }

    // Demi-étoile
    if (hasHalfStar) {
      stars.push('half');
    }

    // Étoiles vides
    const emptyStars = 5 - stars.length;
    for (let i = 0; i < emptyStars; i++) {
      stars.push('empty');
    }

    return stars;
  }
}
