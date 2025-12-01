package com.rental.notification.service;

import com.rental.notification.dto.NotificationRequest;
import com.rental.notification.dto.NotificationResponse;
import com.rental.notification.entity.Notification;
import com.rental.notification.exception.NotificationException;
import com.rental.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    /**
     * Créer et envoyer une notification
     */
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Création d'une notification pour l'utilisateur: {}", request.getUserId());

        // Créer la notification en base
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .reservationId(request.getReservationId())
                .bookingRequestId(request.getBookingRequestId())
                .notificationType(request.getNotificationType())
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .sentViaEmail(false)
                .sentViaSms(false)
                .build();

        // Envoyer l'email si demandé
        if (Boolean.TRUE.equals(request.getSendEmail()) && request.getRecipientEmail() != null) {
            try {
                emailService.sendSimpleEmail(
                        request.getRecipientEmail(),
                        request.getTitle(),
                        request.getMessage()
                );
                notification.setSentViaEmail(true);
                log.info("Email envoyé à: {}", request.getRecipientEmail());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
            }
        }

        // Envoyer le SMS si demandé
        if (Boolean.TRUE.equals(request.getSendSms()) && request.getRecipientPhone() != null) {
            try {
                smsService.sendSms(com.rental.notification.dto.SmsRequest.builder()
                        .to(request.getRecipientPhone())
                        .message(request.getMessage())
                        .build());
                notification.setSentViaSms(true);
                log.info("SMS envoyé à: {}", request.getRecipientPhone());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi du SMS: {}", e.getMessage());
            }
        }

        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    /**
     * Récupérer toutes les notifications d'un utilisateur
     */
    public List<NotificationResponse> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les notifications non lues
     */
    public List<NotificationResponse> getUnreadNotifications(Integer userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compter les notifications non lues
     */
    public long countUnreadNotifications(Integer userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Marquer une notification comme lue
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException("Notification non trouvée"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marquée comme lue", notificationId);
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsReadForUser(userId);
        log.info("Toutes les notifications de l'utilisateur {} marquées comme lues", userId);
    }

    /**
     * Supprimer une notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("Notification {} supprimée", notificationId);
    }

    // Mapper entity -> DTO
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .reservationId(notification.getReservationId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .sentViaEmail(notification.getSentViaEmail())
                .sentViaSms(notification.getSentViaSms())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}