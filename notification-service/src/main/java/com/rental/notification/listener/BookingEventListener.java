package com.rental.notification.listener;

import com.rental.notification.config.RabbitMQConfig;
import com.rental.notification.dto.NotificationRequest;
import com.rental.notification.enums.NotificationType;
import com.rental.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final NotificationService notificationService;

    /**
     * Écouter les événements de notification depuis RabbitMQ
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(Map<String, Object> event) {
        log.info("Événement de notification reçu: {}", event);

        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId((Integer) event.get("userId"))
                    .reservationId((Integer) event.get("reservationId"))
                    .notificationType(NotificationType.valueOf((String) event.get("type")))
                    .title((String) event.get("title"))
                    .message((String) event.get("message"))
                    .recipientEmail((String) event.get("email"))
                    .recipientPhone((String) event.get("phone"))
                    .sendEmail(true)
                    .sendSms(false)
                    .build();

            notificationService.createNotification(request);
            log.info("Notification traitée avec succès pour userId: {}", request.getUserId());

        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement: {}", e.getMessage());
        }
    }
}
