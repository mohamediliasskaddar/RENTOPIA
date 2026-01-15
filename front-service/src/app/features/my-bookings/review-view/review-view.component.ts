import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ReviewService } from '../../../core/services/review.service';
import { ReviewFormComponent } from '../../review-form/review-form.component';
import { Review } from '../../../core/models/review.model';
import { Store } from '@ngrx/store';
import { selectUserId } from '../../../store/auth/auth.selectors';
import { firstValueFrom } from 'rxjs';
import {MatDivider} from "@angular/material/divider";

@Component({
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatDivider
  ],
  templateUrl: './review-view.component.html',
  styleUrls: ['./review-view.component.scss']
})
export class ReviewViewComponent {

  review: any;
  loading = true;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { review: any },
    private dialog: MatDialog,
    private reviewService: ReviewService,
    private store: Store
  ) {
    this.review = data.review;
  }

  async onEditReview(review: Review) {
    // 🔹 Récupérer userId depuis le store
    const userId = await firstValueFrom(this.store.select(selectUserId));

    if (!userId) {
      console.error('User ID not found!');
      return;
    }

    const dialogRef = this.dialog.open(ReviewFormComponent, {
      width: '600px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      panelClass: 'review-form-dialog-container', // ✅ CRUCIAL
      backdropClass: 'review-form-dialog-backdrop', // ✅ CRUCIAL
      hasBackdrop: true,
      disableClose: false,
      autoFocus: false,
      data: {
        review,   // passer l'objet review existant
        userId    // ✅ userId correct depuis le store
      }
    });

    dialogRef.afterClosed().subscribe((updated: any) => {
      if (updated) {
        // Crée un nouvel objet pour déclencher le change detection
        this.review = { ...this.review, ...updated };
      }
    });
  }
}



