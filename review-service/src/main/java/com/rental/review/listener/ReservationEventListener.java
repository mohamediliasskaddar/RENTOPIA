package com.rental.review.listener;

import com.rental.review.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    /**
     * Écouter les événements de réservation complétée
     * Cela peut déclencher une demande d'avis automatique
     */
    @RabbitListener(queues = RabbitMQConfig.REVIEW_QUEUE)
    public void handleReservationCompletedEvent(Map<String, Object> event) {
        log.info("Événement de réservation reçu: {}", event);

        try {
            String eventType = (String) event.get("eventType");

            if ("RESERVATION_COMPLETED".equals(eventType)) {
                Integer reservationId = (Integer) event.get("reservationId");
                Integer userId = (Integer) event.get("userId");
                Integer propertyId = (Integer) event.get("propertyId");

                log.info("Réservation {} complétée. Prêt pour un avis.", reservationId);

                // Ici, vous pourriez envoyer une notification via le Notification Service
                // pour demander à l'utilisateur de laisser un avis

            }

        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement: {}", e.getMessage());
        }
    }
}
