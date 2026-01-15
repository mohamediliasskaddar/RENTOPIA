// src/app/store/notification/notification.selectors.ts
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { NotificationState } from './notification.reducer';

export const selectNotificationState = createFeatureSelector<NotificationState>('notification');

export const selectAllNotifications = createSelector(
  selectNotificationState,
  (state) => state.notifications
);

export const selectUnreadNotifications = createSelector(
  selectAllNotifications,
  (notifications) => notifications.filter(n => !n.isRead)
);

export const selectUnreadCount = createSelector(
  selectNotificationState,
  (state) => state.unreadCount
);

export const selectNotificationLoading = createSelector(
  selectNotificationState,
  (state) => state.loading
);

export const selectRecentNotifications = createSelector(
  selectAllNotifications,
  (notifications) => notifications.slice(0, 5)
);
