// src/app/features/notifications/notifications-page/notifications-page.component.ts
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatRippleModule } from '@angular/material/core';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import {
  Notification,
  NotificationType,
  getNotificationIcon,
  getNotificationColor,
  getNotificationTypeLabel,
  getNotificationRoute,
  getTimeAgo
} from '../../../../core/models/notification.model';
import * as NotificationActions from '../../../../store/notification/notification.actions';
import {
  selectAllNotifications,
  selectUnreadNotifications,
  selectUnreadCount,
  selectNotificationLoading
} from '../../../../store/notification/notification.selectors';
import { selectUserId, selectIsHost } from '../../../../store/auth/auth.selectors';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatTooltipModule,
    MatRippleModule
  ],
  templateUrl: './notifications-page.component.html',
  styleUrl: './notifications-page.component.scss'
})
export class NotificationsPageComponent implements OnInit, OnDestroy {
  private store = inject(Store);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  userId: number | null = null;
  isHost = false;
  allNotifications: Notification[] = [];
  unreadNotifications: Notification[] = [];
  unreadCount = 0;
  loading = false;

  selectedTab = 0;

  // Helpers
  getIcon = getNotificationIcon;
  getColor = getNotificationColor;
  getTypeLabel = getNotificationTypeLabel;
  getTimeAgo = getTimeAgo;

  ngOnInit(): void {
    this.store.select(selectUserId).pipe(
      takeUntil(this.destroy$)
    ).subscribe(userId => {
      this.userId = userId;
      if (userId) {
        this.store.dispatch(NotificationActions.loadNotifications({ userId }));
      }
    });

    this.store.select(selectIsHost).pipe(
      takeUntil(this.destroy$)
    ).subscribe(isHost => this.isHost = isHost);

    this.store.select(selectAllNotifications).pipe(
      takeUntil(this.destroy$)
    ).subscribe(notifications => this.allNotifications = notifications);

    this.store.select(selectUnreadNotifications).pipe(
      takeUntil(this.destroy$)
    ).subscribe(notifications => this.unreadNotifications = notifications);

    this.store.select(selectUnreadCount).pipe(
      takeUntil(this.destroy$)
    ).subscribe(count => this.unreadCount = count);

    this.store.select(selectNotificationLoading).pipe(
      takeUntil(this.destroy$)
    ).subscribe(loading => this.loading = loading);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get currentNotifications(): Notification[] {
    return this.selectedTab === 0 ? this.allNotifications : this.unreadNotifications;
  }

  /**
   * ✅ MODIFIÉ : Navigation avec state
   */
  onNotificationClick(notification: Notification): void {
    console.log('🔔 Notification clicked:', notification);

    // Marquer comme lue
    if (!notification.isRead) {
      this.store.dispatch(NotificationActions.markAsRead({
        notificationId: notification.id
      }));
    }

    // Récupérer la navigation
    const navData = getNotificationRoute(notification, this.isHost);
    console.log('📍 Navigation data:', navData);

    if (navData) {
      // Naviguer avec state si présent
      if (navData.state) {
        console.log('🚀 Navigating with state:', navData.state);
        this.router.navigate(navData.route, {
          state: navData.state,
          replaceUrl: false  // ✅ Important: ne pas remplacer l'URL
        }).then(success => {
          console.log('✅ Navigation completed:', success);
          if (!success) {
            console.error('❌ Navigation failed');
          }
        });
      } else {
        this.router.navigate(navData.route);
      }
    }
  }

  markAsRead(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.store.dispatch(NotificationActions.markAsRead({
      notificationId: notification.id
    }));
  }

  markAllAsRead(): void {
    if (this.userId) {
      this.store.dispatch(NotificationActions.markAllAsRead({
        userId: this.userId
      }));
    }
  }

  deleteNotification(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.store.dispatch(NotificationActions.deleteNotification({
      notificationId: notification.id
    }));
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  trackById(index: number, notification: Notification): number {
    return notification.id;
  }
}
