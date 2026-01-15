// src/app/store/notification/notification.reducer.ts
import { createReducer, on } from '@ngrx/store';
import { Notification } from '../../core/models/notification.model';
import * as NotificationActions from './notification.actions';

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  loading: boolean;
  error: string | null;
}

export const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  loading: false,
  error: null
};

export const notificationReducer = createReducer(
  initialState,

  // Load notifications
  on(NotificationActions.loadNotifications, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(NotificationActions.loadNotificationsSuccess, (state, { notifications }) => ({
    ...state,
    notifications,
    unreadCount: notifications.filter(n => !n.isRead).length,
    loading: false
  })),

  on(NotificationActions.loadNotificationsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Load unread count
  on(NotificationActions.loadUnreadCountSuccess, (state, { count }) => ({
    ...state,
    unreadCount: count
  })),

  // Mark as read
  on(NotificationActions.markAsReadSuccess, (state, { notificationId }) => ({
    ...state,
    notifications: state.notifications.map(n =>
      n.id === notificationId ? { ...n, isRead: true } : n
    ),
    unreadCount: Math.max(0, state.unreadCount - 1)
  })),

  // Mark all as read
  on(NotificationActions.markAllAsReadSuccess, (state) => ({
    ...state,
    notifications: state.notifications.map(n => ({ ...n, isRead: true })),
    unreadCount: 0
  })),

  // Delete
  on(NotificationActions.deleteNotificationSuccess, (state, { notificationId }) => {
    const deleted = state.notifications.find(n => n.id === notificationId);
    return {
      ...state,
      notifications: state.notifications.filter(n => n.id !== notificationId),
      unreadCount: deleted && !deleted.isRead ? state.unreadCount - 1 : state.unreadCount
    };
  }),

  // Add new
  on(NotificationActions.addNotification, (state, { notification }) => ({
    ...state,
    notifications: [notification, ...state.notifications],
    unreadCount: state.unreadCount + 1
  })),

  // Clear
  on(NotificationActions.clearNotifications, () => initialState)
);
