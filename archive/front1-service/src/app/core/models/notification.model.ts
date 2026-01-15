// src/app/core/models/notification.model.ts

/**
 * Types de notifications
 */
export enum NotificationType {
  BOOKING_CONFIRMATION = 'BOOKING_CONFIRMATION',
  BOOKING_REMINDER = 'BOOKING_REMINDER',
  BOOKING_CANCELLED = 'BOOKING_CANCELLED',
  PAYMENT_RECEIVED = 'PAYMENT_RECEIVED',
  PAYMENT_FAILED = 'PAYMENT_FAILED',
  CHECK_IN_REMINDER = 'CHECK_IN_REMINDER',
  CHECK_OUT_REMINDER = 'CHECK_OUT_REMINDER',
  REVIEW_REQUEST = 'REVIEW_REQUEST',
  MESSAGE_RECEIVED = 'MESSAGE_RECEIVED',
  BOOKING_REQUEST_RECEIVED = 'BOOKING_REQUEST_RECEIVED',
  BOOKING_REQUEST_ACCEPTED = 'BOOKING_REQUEST_ACCEPTED',
  BOOKING_REQUEST_DECLINED = 'BOOKING_REQUEST_DECLINED'
}

/**
 * Notification Response DTO
 */
export interface Notification {
  id: number;
  userId: number;
  reservationId?: number;
  bookingRequestId?: number;
  notificationType: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  sentViaEmail: boolean;
  sentViaSms: boolean;
  createdAt: string;
}

/**
 * Pour créer une notification
 */
export interface CreateNotificationDTO {
  userId: number;
  reservationId?: number;
  bookingRequestId?: number;
  notificationType: NotificationType;
  title: string;
  message: string;
  recipientEmail?: string;
  recipientPhone?: string;
  sendEmail?: boolean;
  sendSms?: boolean;
}

/**
 * Get notification icon
 */
export function getNotificationIcon(type: NotificationType): string {
  const icons: Record<NotificationType, string> = {
    [NotificationType.BOOKING_CONFIRMATION]: 'check_circle',
    [NotificationType.BOOKING_REMINDER]: 'event',
    [NotificationType.BOOKING_CANCELLED]: 'cancel',
    [NotificationType.PAYMENT_RECEIVED]: 'payments',
    [NotificationType.PAYMENT_FAILED]: 'error',
    [NotificationType.CHECK_IN_REMINDER]: 'login',
    [NotificationType.CHECK_OUT_REMINDER]: 'logout',
    [NotificationType.REVIEW_REQUEST]: 'star',
    [NotificationType.MESSAGE_RECEIVED]: 'chat',
    [NotificationType.BOOKING_REQUEST_RECEIVED]: 'notification_important',
    [NotificationType.BOOKING_REQUEST_ACCEPTED]: 'thumb_up',
    [NotificationType.BOOKING_REQUEST_DECLINED]: 'thumb_down'
  };
  return icons[type] || 'notifications';
}

/**
 * Get notification color
 */
export function getNotificationColor(type: NotificationType): string {
  const colors: Record<NotificationType, string> = {
    [NotificationType.BOOKING_CONFIRMATION]: '#4CAF50',
    [NotificationType.BOOKING_REMINDER]: '#2196F3',
    [NotificationType.BOOKING_CANCELLED]: '#f44336',
    [NotificationType.PAYMENT_RECEIVED]: '#4CAF50',
    [NotificationType.PAYMENT_FAILED]: '#f44336',
    [NotificationType.CHECK_IN_REMINDER]: '#FF9800',
    [NotificationType.CHECK_OUT_REMINDER]: '#FF9800',
    [NotificationType.REVIEW_REQUEST]: '#9C27B0',
    [NotificationType.MESSAGE_RECEIVED]: '#00BCD4',
    [NotificationType.BOOKING_REQUEST_RECEIVED]: '#FF5722',
    [NotificationType.BOOKING_REQUEST_ACCEPTED]: '#4CAF50',
    [NotificationType.BOOKING_REQUEST_DECLINED]: '#f44336'
  };
  return colors[type] || '#666';
}

/**
 * Get time ago string
 */
export function getTimeAgo(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (seconds < 60) return 'Just now';
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;
  return date.toLocaleDateString();
}

/**
 * ✅ NOUVEAU : Get navigation route based on notification type
 */
// src/app/core/models/notification.model.ts

export function getNotificationRoute(
  notification: Notification,
  isHost: boolean
): NotificationNavigationData | null {
  const { notificationType, reservationId } = notification;

  switch (notificationType) {
    // Guest booking notifications - Ouvrir le dialog directement
    case NotificationType.BOOKING_CONFIRMATION:
    case NotificationType.BOOKING_REMINDER:
    case NotificationType.PAYMENT_FAILED:
    case NotificationType.CHECK_IN_REMINDER:
    case NotificationType.CHECK_OUT_REMINDER:
    case NotificationType.BOOKING_REQUEST_ACCEPTED:
    case NotificationType.BOOKING_REQUEST_DECLINED:
      if (reservationId) {
        return {
          route: ['/my-bookings'],
          state: { openBookingDialog: reservationId }
        };
      }
      return { route: ['/my-bookings'] };

    // Host notifications
    case NotificationType.PAYMENT_RECEIVED:
    case NotificationType.BOOKING_REQUEST_RECEIVED:
    case NotificationType.BOOKING_CANCELLED:
      if (reservationId) {
        return {
          route: ['/host/bookings', reservationId.toString()]
        };
      }
      return { route: ['/host/bookings'] };

    // Review request
    case NotificationType.REVIEW_REQUEST:
      return { route: ['/reviews'] };

    // Message received
    case NotificationType.MESSAGE_RECEIVED:
      return { route: ['/messages'] };

    default:
      return { route: ['/notifications'] };
  }
}
/**
 * ✅ NOUVEAU : Get notification type label
 */
export function getNotificationTypeLabel(type: NotificationType): string {
  const labels: Record<NotificationType, string> = {
    [NotificationType.BOOKING_CONFIRMATION]: 'Booking Confirmed',
    [NotificationType.BOOKING_REMINDER]: 'Reminder',
    [NotificationType.BOOKING_CANCELLED]: 'Booking Cancelled',
    [NotificationType.PAYMENT_RECEIVED]: 'Payment Received',
    [NotificationType.PAYMENT_FAILED]: 'Payment Failed',
    [NotificationType.CHECK_IN_REMINDER]: 'Check-in Reminder',
    [NotificationType.CHECK_OUT_REMINDER]: 'Check-out Reminder',
    [NotificationType.REVIEW_REQUEST]: 'Review Request',
    [NotificationType.MESSAGE_RECEIVED]: 'New Message',
    [NotificationType.BOOKING_REQUEST_RECEIVED]: 'New Booking Request',
    [NotificationType.BOOKING_REQUEST_ACCEPTED]: 'Request Accepted',
    [NotificationType.BOOKING_REQUEST_DECLINED]: 'Request Declined'
  };
  return labels[type] || 'Notification';
}

export interface NotificationNavigationData {
  route: string[];
  state?: any;
}
