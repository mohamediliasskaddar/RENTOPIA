// src/app/core/services/review.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PropertyReviewStats } from '../models/review-stats.model';
import { Review, ReviewRequest, ReviewUpdateRequest } from "../models/review.model";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: 'root' })
export class ReviewService {

  private readonly baseUrl = `${environment.apiUrl}${environment.services.review}`;

  constructor(private http: HttpClient) {
    console.log('✅ ReviewService initialized with baseUrl:', this.baseUrl);
  }

  createReview(request: ReviewRequest): Observable<Review> {
    return this.http.post<Review>(this.baseUrl, request);
  }

  getPropertyReviews(propertyId: number): Observable<Review[]> {
    const url = `${this.baseUrl}/api/property/${propertyId}`;
    console.log('✅ Fetching reviews from:', url);
    return this.http.get<Review[]>(url);
  }

  getPropertyStats(propertyId: number): Observable<PropertyReviewStats> {
    const url = `${this.baseUrl}/api/property/${propertyId}/stats`;
    console.log('✅ Fetching stats from:', url);
    return this.http.get<PropertyReviewStats>(url);
  }

  getAverageRating(propertyId: number): Observable<{ averageRating: number }> {
    return this.http.get<{ averageRating: number }>(
      `${this.baseUrl}/property/${propertyId}/average-rating`
    );
  }

  updateReview(id: number, request: ReviewUpdateRequest, userId: number): Observable<Review> {
    const params = new HttpParams().set('userId', userId);
    return this.http.put<Review>(`${this.baseUrl}/${id}`, request, { params });
  }

  /**
   * ✅ CORRIGÉ : Retourner le Rating complet au lieu de text
   */
  updateRating(reviewId: number, ratingValue: number): Observable<any> {
    console.log(`🔄 Updating rating for review ${reviewId} to ${ratingValue}`);

    const params = new HttpParams().set('ratingValue', ratingValue.toString());

    return this.http.put<any>(
      `${this.baseUrl}/${reviewId}/rating`,
      {}, // corps vide
      { params }
    );
  }

  deleteReview(id: number, userId: number): Observable<any> {
    const params = new HttpParams().set('userId', userId);
    return this.http.delete(`${this.baseUrl}/${id}`, { params });
  }

  getUserReviews(userId: number): Observable<Review[]> {
    const url = `${this.baseUrl}/user/${userId}`;
    console.log('✅ Fetching user reviews from:', url);
    return this.http.get<Review[]>(url);
  }

  getReviewByReservationId(reservationId: number): Observable<Review> {
    const url = `${this.baseUrl}/reservation/${reservationId}`;
    console.log('✅ Fetching review by reservationId from:', url);
    return this.http.get<Review>(url);
  }

  getReviewById(reviewId: number): Observable<Review> {
    return this.http.get<Review>(`${this.baseUrl}/${reviewId}`);
  }
}
