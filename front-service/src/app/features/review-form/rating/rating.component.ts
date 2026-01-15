// src/app/shared/components/rating/rating.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="rating-container">
      <div class="stars">
        <mat-icon
          *ngFor="let star of starsArray; let i = index"
          [class.active]="i < currentRating"
          [class.hover]="i < hoverRating"
          (click)="setRating(i + 1)"
          (mouseenter)="setHoverRating(i + 1)"
          (mouseleave)="clearHoverRating()">
          {{ getStarIcon(i) }}
        </mat-icon>
      </div>
    </div>
  `,
  styles: [`
    .rating-container {
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .stars {
      display: flex;
      gap: 4px;
      cursor: pointer;
    }

    mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #ddd;
      transition: color 0.2s ease, transform 0.2s ease;

      &:hover {
        transform: scale(1.1);
      }

      &.active {
        color: #ffb400;
      }

      &.hover {
        color: #ffd700;
      }
    }

    @media (max-width: 600px) {
      mat-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
      }
    }
  `]
})
export class RatingComponent {
  @Input() rating = 0;
  @Output() ratingChange = new EventEmitter<number>();

  starsArray = [1, 2, 3, 4, 5];
  currentRating = 0;
  hoverRating = 0;

  ngOnInit() {
    this.currentRating = this.rating;
  }

  ngOnChanges() {
    this.currentRating = this.rating;
  }

  setRating(rating: number): void {
    this.currentRating = rating;
    this.ratingChange.emit(rating);
  }

  setHoverRating(rating: number): void {
    this.hoverRating = rating;
  }

  clearHoverRating(): void {
    this.hoverRating = 0;
  }

  getStarIcon(index: number): string {
    const rating = this.hoverRating || this.currentRating;
    return index < rating ? 'star' : 'star_border';
  }
}
