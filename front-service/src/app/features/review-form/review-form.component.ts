// src/app/features/reviews/components/review-form/review-form.component.ts

import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { ReviewRequest, ReviewUpdateRequest, Review } from '../../core/models/review.model';
import { ApiService } from '../../core/services/api.service';
import { ReviewService } from '../../core/services/review.service';
import { BookingWithSnapshot } from '../../core/models/booking-with-snapshot.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-review-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './review-form.component.html',
  styleUrls: ['./review-form.component.scss']
})
export class ReviewFormComponent implements OnInit {

  reviewForm: FormGroup;
  loading = false;
  rating = 0;
  isEditMode = false;

  stars = [1, 2, 3, 4, 5];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ReviewFormComponent>,
    private snackBar: MatSnackBar,
    private apiService: ApiService,
    private reviewService: ReviewService,
    @Inject(MAT_DIALOG_DATA) public data: {
      booking?: BookingWithSnapshot;
      userId: number;
      review?: Review;
    }
  ) {
    this.reviewForm = this.fb.group({
      reviewText: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
      ratingValue: [0, [Validators.required, Validators.min(1), Validators.max(5)]]
    });
  }

  ngOnInit(): void {
    if (this.data.review) {
      this.isEditMode = true;
      console.log('📝 Edit mode - Review:', this.data.review);

      // Pré-remplir le formulaire
      this.reviewForm.patchValue({
        reviewText: this.data.review.reviewText,
        ratingValue: this.data.review.ratingValue
      });
      this.rating = this.data.review.ratingValue;
    }
  }

  onRatingChange(rating: number): void {
    console.log('⭐ Rating changed to:', rating);
    this.rating = rating;
    this.reviewForm.patchValue({ ratingValue: rating });
  }

  onSubmit(): void {
    if (this.reviewForm.invalid || this.rating === 0) {
      this.snackBar.open('Please provide a rating and write a review', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;

    if (this.isEditMode && this.data.review) {
      console.log('🔄 Updating review:', this.data.review.id);
      this.updateExistingReview();
    } else {
      console.log('✨ Creating new review');
      this.createNewReview();
    }
  }

  /**
   * ✅ CORRIGÉ : Mise à jour avec récupération du review complet
   */
  private updateExistingReview(): void {
    const updateRequest: ReviewUpdateRequest = {
      reviewText: this.reviewForm.value.reviewText
    };

    const reviewId = this.data.review!.id;
    const newRating = this.rating;

    console.log('📤 Updating review text and rating:', {
      reviewId,
      text: updateRequest.reviewText,
      rating: newRating
    });

    // ✅ Effectuer les deux mises à jour en parallèle
    forkJoin({
      reviewUpdate: this.reviewService.updateReview(reviewId, updateRequest, this.data.userId),
      ratingUpdate: this.reviewService.updateRating(reviewId, newRating)
    }).subscribe({
      next: ({ reviewUpdate, ratingUpdate }) => {
        console.log('✅ Review updated:', reviewUpdate);
        console.log('✅ Rating updated:', ratingUpdate);

        // ✅ Récupérer le review complet mis à jour
        this.reviewService.getReviewById(reviewId).subscribe({
          next: (updatedReview) => {
            console.log('✅ Full updated review:', updatedReview);

            this.dialogRef.close(updatedReview);
            this.snackBar.open('Review updated successfully!', 'Close', { duration: 3000 });
            this.loading = false;
          },
          error: (err) => {
            console.error('❌ Error fetching updated review:', err);
            // Fermer quand même avec les données qu'on a
            this.dialogRef.close({
              ...this.data.review,
              reviewText: this.reviewForm.value.reviewText,
              ratingValue: newRating
            });
            this.snackBar.open('Review updated!', 'Close', { duration: 3000 });
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error('❌ Error updating review:', err);
        this.snackBar.open(err.message || 'Error updating review', 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  /**
   * Création d'un nouveau review
   */
  private createNewReview(): void {
    if (!this.data.booking) {
      this.snackBar.open('Booking information missing', 'Close', { duration: 3000 });
      this.loading = false;
      return;
    }

    const createRequest: ReviewRequest = {
      reservationId: this.data.booking.bookingId,
      userId: this.data.userId,
      propertyId: this.data.booking.propertyId,
      reviewText: this.reviewForm.value.reviewText,
      ratingValue: this.rating
    };

    console.log('📤 Creating review:', createRequest);

    this.apiService.post<Review>('/reviews', createRequest).subscribe({
      next: (newReview) => {
        console.log('✅ Review created:', newReview);
        this.dialogRef.close(newReview);
        this.snackBar.open('Thank you for your review!', 'Close', { duration: 3000 });
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Error creating review:', error);
        this.snackBar.open(error.message || 'Error submitting review. Please try again.', 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
