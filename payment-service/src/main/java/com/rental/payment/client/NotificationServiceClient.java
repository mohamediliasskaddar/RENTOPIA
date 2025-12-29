package com.rental.payment.client;

import com.rental.payment.config.FeignConfig;
import com.rental.payment.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "NOTIFICATION-SERVICE",
        configuration = FeignConfig.class,
        fallback = NotificationServiceClientFallback.class
)
public interface NotificationServiceClient {

    // ✅ Retourne Map au lieu de ResponseEntity<Void>
    @PostMapping("/api/notifications")
    Map<String, Object> createNotification(@RequestBody NotificationRequest request);

    // ✅ Retourne List<Map> au lieu de ResponseEntity<List<Map>>
    @GetMapping("/api/notifications/user/{userId}")
    Object getUserNotifications(
            @PathVariable("userId") Integer userId
    );

    @GetMapping("/api/notifications/user/{userId}/unread")
    List<Map<String, Object>> getUnreadNotifications(
            @PathVariable("userId") Integer userId
    );

    // ✅ Retourne Map au lieu de ResponseEntity<Map>
    @GetMapping("/api/notifications/user/{userId}/unread-count")
    Map<String, Long> countUnreadNotifications(
            @PathVariable("userId") Integer userId
    );

    @PutMapping("/api/notifications/{id}/read")
    Map<String, String> markAsRead(
            @PathVariable("id") Long id
    );

    @PutMapping("/api/notifications/user/{userId}/read-all")
    Map<String, String> markAllAsRead(
            @PathVariable("userId") Integer userId
    );

    @DeleteMapping("/api/notifications/{id}")
    Map<String, String> deleteNotification(
            @PathVariable("id") Long id
    );

    // ✅ TRÈS IMPORTANT : Retourne Map au lieu de ResponseEntity<Map>
    @GetMapping("/api/notifications/health")
    Map<String, Object> healthCheck();
}