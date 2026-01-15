// src/app/store/notification/notification.actions.ts
import { createAction, props } from '@ngrx/store';
import { Notification } from '../../core/models/notification.model';

// Load notifications
export const loadNotifications = createAction(
  '[Notification] Load Notifications',
  props<{ userId: number }>()
);

export const loadNotificationsSuccess = createAction(
  '[Notification] Load Notifications Success',
  props<{ notifications: Notification[] }>()
);

export const loadNotificationsFailure = createAction(
  '[Notification] Load Notifications Failure',
  props<{ error: string }>()
);

// Load unread count
export const loadUnreadCount = createAction(
  '[Notification] Load Unread Count',
  props<{ userId: number }>()
);

export const loadUnreadCountSuccess = createAction(
  '[Notification] Load Unread Count Success',
  props<{ count: number }>()
);

// Mark as read
export const markAsRead = createAction(
  '[Notification] Mark As Read',
  props<{ notificationId: number }>()
);

export const markAsReadSuccess = createAction(
  '[Notification] Mark As Read Success',
  props<{ notificationId: number }>()
);

// Mark all as read
export const markAllAsRead = createAction(
  '[Notification] Mark All As Read',
  props<{ userId: number }>()
);

export const markAllAsReadSuccess = createAction(
  '[Notification] Mark All As Read Success'
);

// Delete notification
export const deleteNotification = createAction(
  '[Notification] Delete',
  props<{ notificationId: number }>()
);

export const deleteNotificationSuccess = createAction(
  '[Notification] Delete Success',
  props<{ notificationId: number }>()
);

// Add new notification (from WebSocket/polling)
export const addNotification = createAction(
  '[Notification] Add New',
  props<{ notification: Notification }>()
);

// Clear
export const clearNotifications = createAction('[Notification] Clear');
