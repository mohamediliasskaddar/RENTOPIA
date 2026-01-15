// src/app/features/reviews/components/review-list/review-list.component.ts
// ✅ Modifié : displayLimit = 3

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Review } from '../../../../core/models/review.model';
import { PropertyReviewStats } from '../../../../core/models/review-stats.model';
import { ReviewCardComponent } from '../review-card/review-card.component';
import { RatingStarsComponent } from '../rating-stars/rating-stars.component';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    RatingStarsComponent,
    ReviewCardComponent
  ],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.scss'
})
export class ReviewListComponent {
  @Input() reviews: Review[] = [];
  @Input() stats: PropertyReviewStats | null = null;
  @Input() loading: boolean = false;
  @Input() currentUserId?: number;

  @Output() editReview = new EventEmitter<Review>();
  @Output() deleteReview = new EventEmitter<number>();
  @Output() loadMore = new EventEmitter<void>();

  // ✅ MODIFIÉ : Limiter à 3 reviews au lieu de 5
  displayLimit = 3;
  showAll = false;

  get displayedReviews(): Review[] {
    if (this.showAll) {
      return this.reviews;
    }
    return this.reviews.slice(0, this.displayLimit);
  }

  get hasMore(): boolean {
    return this.reviews.length > this.displayLimit && !this.showAll;
  }

  toggleShowAll(): void {
    this.showAll = !this.showAll;
  }

  canEditReview(review: Review): boolean {
    return review.userId === this.currentUserId;
  }

  canDeleteReview(review: Review): boolean {
    return review.userId === this.currentUserId;
  }

  onEditReview(review: Review): void {
    this.editReview.emit(review);
  }

  onDeleteReview(reviewId: number): void {
    this.deleteReview.emit(reviewId);
  }

  trackByReviewId(index: number, review: Review): number {
    return review.id;
  }

  getRatingDistribution(stars: number): number {
    if (!this.stats || this.stats.totalReviews === 0) return 0;

    let count = 0;
    switch (stars) {
      case 5: count = this.stats.fiveStarCount; break;
      case 4: count = this.stats.fourStarCount; break;
      case 3: count = this.stats.threeStarCount; break;
      case 2: count = this.stats.twoStarCount; break;
      case 1: count = this.stats.oneStarCount; break;
    }

    return (count / this.stats.totalReviews) * 100;
  }
}
