// src/app/core/services/notification.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification, CreateNotificationDTO } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/notifications'; // Via API Gateway

  /**
   * Headers avec token
   */
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  /**
   * Récupérer toutes les notifications d'un utilisateur
   * GET /api/notifications/user/{userId}
   */
  getUserNotifications(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/user/${userId}`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Récupérer les notifications non lues
   * GET /api/notifications/user/{userId}/unread
   */
  getUnreadNotifications(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/user/${userId}/unread`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Compter les notifications non lues
   * GET /api/notifications/user/{userId}/unread-count
   */
  getUnreadCount(userId: number): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(
      `${this.baseUrl}/user/${userId}/unread-count`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Marquer une notification comme lue
   * PUT /api/notifications/{id}/read
   */
  markAsRead(notificationId: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.baseUrl}/${notificationId}/read`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * Marquer toutes les notifications comme lues
   * PUT /api/notifications/user/{userId}/read-all
   */
  markAllAsRead(userId: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.baseUrl}/user/${userId}/read-all`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * Supprimer une notification
   * DELETE /api/notifications/{id}
   */
  deleteNotification(notificationId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(
      `${this.baseUrl}/${notificationId}`,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Créer une notification (pour tests)
   * POST /api/notifications
   */
  createNotification(notification: CreateNotificationDTO): Observable<Notification> {
    return this.http.post<Notification>(
      this.baseUrl,
      notification,
      { headers: this.getHeaders() }
    );
  }
}
