// src/app/shared/components/notification-bell/notification-bell.component.ts

import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatRippleModule } from '@angular/material/core';
import { Store } from '@ngrx/store';
import { Subject, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import {
  Notification,
  getNotificationIcon,
  getNotificationColor,
  getNotificationTypeLabel,
  getNotificationRoute,
  getTimeAgo
} from '../../../core/models/notification.model';
import * as NotificationActions from '../../../store/notification/notification.actions';
import {
  selectUnreadCount,
  selectUnreadNotifications,
  selectNotificationLoading
} from '../../../store/notification/notification.selectors';
import { selectUserId, selectIsHost } from '../../../store/auth/auth.selectors';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatMenuModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatRippleModule
  ],
  templateUrl: 'notification-bell.component.html',
  styleUrl: 'notification-bell.component.scss'
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  private store = inject(Store);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  // ✅ Properties utilisées dans le template
  userId: number | null = null;
  isHost = false;
  unreadCount = 0;
  notifications: Notification[] = [];
  loading = false;

  // Helper functions
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
        this.loadNotifications();
        this.startPolling();
      }
    });

    this.store.select(selectIsHost).pipe(
      takeUntil(this.destroy$)
    ).subscribe(isHost => this.isHost = isHost);

    this.store.select(selectUnreadCount).pipe(
      takeUntil(this.destroy$)
    ).subscribe(count => this.unreadCount = count);

    this.store.select(selectUnreadNotifications).pipe(
      takeUntil(this.destroy$)
    ).subscribe(notifications => {
      this.notifications = notifications;
      console.log('📬 Notifications updated:', notifications.length);
    });

    this.store.select(selectNotificationLoading).pipe(
      takeUntil(this.destroy$)
    ).subscribe(loading => this.loading = loading);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadNotifications(): void {
    if (this.userId) {
      this.store.dispatch(NotificationActions.loadNotifications({
        userId: this.userId
      }));
    }
  }

  private startPolling(): void {
    interval(30000).pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      if (this.userId) {
        this.store.dispatch(NotificationActions.loadUnreadCount({
          userId: this.userId
        }));
      }
    });
  }

  onMenuOpened(): void {
    console.log('📂 Menu opened - current state:', {
      notifications: this.notifications.length,
      loading: this.loading,
      userId: this.userId
    });
    this.loadNotifications();
  }

  /**
   * ✅ SOLUTION : Gérer le cas où on est déjà sur la page
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

    if (!navData) return;

    const targetUrl = navData.route.join('/');
    const currentUrl = this.router.url.split('?')[0]; // Enlever les query params

    console.log('🔍 Checking URLs:', { current: currentUrl, target: targetUrl });

    // ✅ CAS 1 : On est déjà sur la page cible
    if (currentUrl === targetUrl) {
      console.log('✅ Already on target page, triggering event directly');

      if (navData.state?.openBookingDialog) {
        // Déclencher un événement personnalisé pour ouvrir le dialog
        window.dispatchEvent(new CustomEvent('openBookingDialog', {
          detail: { bookingId: navData.state.openBookingDialog }
        }));
      }
      return;
    }

    // ✅ CAS 2 : Navigation vers une autre page
    if (navData.state) {
      console.log('🚀 Navigating with state:', navData.state);

      // Utiliser navigateByUrl avec state
      this.router.navigate(navData.route, {
        state: navData.state,
        replaceUrl: false
      }).then(success => {
        console.log('✅ Navigation completed:', success);
        if (!success) {
          console.error('❌ Navigation failed - trying alternative method');
          // Plan B : utiliser window.history directement
          window.history.pushState(navData.state, '', targetUrl);
          window.dispatchEvent(new PopStateEvent('popstate'));
        }
      });
    } else {
      this.router.navigate(navData.route);
    }
  }

  markAllRead(event: Event): void {
    event.stopPropagation();
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

  viewAll(): void {
    this.router.navigate(['/notifications']);
  }

  trackByNotificationId(index: number, notification: Notification): number {
    return notification.id;
  }

  get badgeCount(): string | null {
    if (this.unreadCount <= 0) return null;
    return this.unreadCount > 99 ? '99+' : String(this.unreadCount);
  }
}
