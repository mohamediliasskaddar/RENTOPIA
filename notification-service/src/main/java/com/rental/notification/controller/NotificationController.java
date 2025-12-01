package com.rental.notification.controller;

import com.rental.notification.dto.NotificationRequest;
import com.rental.notification.dto.NotificationResponse;
import com.rental.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    /**
     * Créer une nouvelle notification
     * POST http://localhost:8086/api/notifications
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        log.info("Requête de création de notification reçue pour userId: {}", request.getUserId());
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer toutes les notifications d'un utilisateur
     * GET http://localhost:8086/api/notifications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable Integer userId) {
        log.info("Récupération des notifications pour userId: {}", userId);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Récupérer les notifications non lues
     * GET http://localhost:8086/api/notifications/user/{userId}/unread
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @PathVariable Integer userId) {
        log.info("Récupération des notifications non lues pour userId: {}", userId);
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Compter les notifications non lues
     * GET http://localhost:8086/api/notifications/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications(
            @PathVariable Integer userId) {
        log.info("Comptage des notifications non lues pour userId: {}", userId);
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Marquer une notification comme lue
     * PUT http://localhost:8086/api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        log.info("Marquage de la notification {} comme lue", id);
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
    }

    /**
     * Marquer toutes les notifications comme lues
     * PUT http://localhost:8086/api/notifications/user/{userId}/read-all
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Integer userId) {
        log.info("Marquage de toutes les notifications comme lues pour userId: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "Toutes les notifications marquées comme lues"));
    }

    /**
     * Supprimer une notification
     * DELETE http://localhost:8086/api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        log.info("Suppression de la notification {}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification supprimée avec succès"));
    }

    /**
     * Health check
     * GET http://localhost:8086/api/notifications/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Notification Service",
                "port", "8086"
        ));
    }
}