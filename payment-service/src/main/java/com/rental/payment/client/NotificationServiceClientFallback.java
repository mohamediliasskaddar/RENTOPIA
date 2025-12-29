package com.rental.payment.client;

import com.rental.payment.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NotificationServiceClientFallback implements NotificationServiceClient {

    @Override
    public Map<String, Object> createNotification(NotificationRequest request) {
        log.warn("⚠️ Fallback: Notification Service unavailable for createNotification. " +
                "Type: {}, UserId: {}", request.getNotificationType(), request.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "FALLBACK");
        response.put("message", "Notification non envoyée (service temporairement indisponible)");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @Override
    public Object getUserNotifications(Integer userId) {
        log.warn("⚠️ Fallback: Notification Service unavailable for getUserNotifications - User: {}", userId);
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getUnreadNotifications(Integer userId) {
        log.warn("⚠️ Fallback: Notification Service unavailable for getUnreadNotifications - User: {}", userId);
        return List.of();
    }

    @Override
    public Map<String, Long> countUnreadNotifications(Integer userId) {
        log.warn("⚠️ Fallback: Notification Service unavailable for countUnreadNotifications - User: {}", userId);
        return Map.of("unreadCount", 0L);
    }

    @Override
    public Map<String, String> markAsRead(Long id) {
        log.warn("⚠️ Fallback: Notification Service unavailable for markAsRead - ID: {}", id);
        return Map.of("message", "Fallback: Mark as read skipped");
    }

    @Override
    public Map<String, String> markAllAsRead(Integer userId) {
        log.warn("⚠️ Fallback: Notification Service unavailable for markAllAsRead - User: {}", userId);
        return Map.of("message", "Fallback: Mark all as read skipped");
    }

    @Override
    public Map<String, String> deleteNotification(Long id) {
        log.warn("⚠️ Fallback: Notification Service unavailable for deleteNotification - ID: {}", id);
        return Map.of("message", "Fallback: Delete notification skipped");
    }

    @Override
    public Map<String, Object> healthCheck() {
        log.warn("⚠️ Fallback: Notification Service health check unavailable");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "FALLBACK");
        response.put("service", "notification-service");
        response.put("message", "Service temporairement indisponible");
        response.put("timestamp", LocalDateTime.now());

        return response;
    }
}