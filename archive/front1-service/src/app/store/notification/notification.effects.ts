// src/app/store/notification/notification.effects.ts
import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, exhaustMap, tap } from 'rxjs/operators';
import { NotificationService } from '../../core/services/notification.service';
import * as NotificationActions from './notification.actions';

@Injectable()
export class NotificationEffects {
  private actions$ = inject(Actions);
  private notificationService = inject(NotificationService);

  loadNotifications$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotificationActions.loadNotifications),
      exhaustMap(({ userId }) =>
        this.notificationService.getUserNotifications(userId).pipe(
          map(notifications => NotificationActions.loadNotificationsSuccess({ notifications })),
          catchError(error => of(NotificationActions.loadNotificationsFailure({
            error: error.message || 'Failed to load notifications'
          })))
        )
      )
    )
  );

  loadUnreadCount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotificationActions.loadUnreadCount),
      exhaustMap(({ userId }) =>
        this.notificationService.getUnreadCount(userId).pipe(
          map(response => NotificationActions.loadUnreadCountSuccess({ count: response.unreadCount })),
          catchError(() => of(NotificationActions.loadUnreadCountSuccess({ count: 0 })))
        )
      )
    )
  );

  markAsRead$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotificationActions.markAsRead),
      exhaustMap(({ notificationId }) =>
        this.notificationService.markAsRead(notificationId).pipe(
          map(() => NotificationActions.markAsReadSuccess({ notificationId })),
          catchError(() => of({ type: 'NOOP' }))
        )
      )
    )
  );

  markAllAsRead$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotificationActions.markAllAsRead),
      exhaustMap(({ userId }) =>
        this.notificationService.markAllAsRead(userId).pipe(
          map(() => NotificationActions.markAllAsReadSuccess()),
          catchError(() => of({ type: 'NOOP' }))
        )
      )
    )
  );

  deleteNotification$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotificationActions.deleteNotification),
      exhaustMap(({ notificationId }) =>
        this.notificationService.deleteNotification(notificationId).pipe(
          map(() => NotificationActions.deleteNotificationSuccess({ notificationId })),
          catchError(() => of({ type: 'NOOP' }))
        )
      )
    )
  );
}
