package com.rentaldapp.bookingservice.service;

import com.rentaldapp.bookingservice.config.RabbitMQConfig;
import com.rentaldapp.bookingservice.model.dto.ReservationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour publier des événements de réservation sur RabbitMQ
 * Ces événements seront consommés par d'autres microservices
 */
@Service
public class BookingEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Publier un événement de création de réservation
     */
    public void publishBookingCreated(ReservationResponseDTO reservation) {
        try {
            Map<String, Object> event = createBookingEvent(reservation, "BOOKING_CREATED");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_CREATED_KEY,
                    event
            );

            logger.info("✅ Event BOOKING_CREATED published for reservation {}", reservation.getId());
        } catch (Exception e) {
            logger.error("❌ Failed to publish BOOKING_CREATED event", e);
        }
    }

    /**
     * Publier un événement de confirmation de réservation
     */
    public void publishBookingConfirmed(ReservationResponseDTO reservation) {
        try {
            Map<String, Object> event = createBookingEvent(reservation, "BOOKING_CONFIRMED");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_CONFIRMED_KEY,
                    event
            );

            logger.info("✅ Event BOOKING_CONFIRMED published for reservation {}", reservation.getId());
        } catch (Exception e) {
            logger.error("❌ Failed to publish BOOKING_CONFIRMED event", e);
        }
    }

    /**
     * Publier un événement d'annulation de réservation
     */
    public void publishBookingCancelled(ReservationResponseDTO reservation, String reason) {
        try {
            Map<String, Object> event = createBookingEvent(reservation, "BOOKING_CANCELLED");
            event.put("cancellationReason", reason);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_CANCELLED_KEY,
                    event
            );

            logger.info("✅ Event BOOKING_CANCELLED published for reservation {}", reservation.getId());
        } catch (Exception e) {
            logger.error("❌ Failed to publish BOOKING_CANCELLED event", e);
        }
    }

    /**
     * Publier un événement de réservation terminée (check-out)
     */
    public void publishBookingCompleted(ReservationResponseDTO reservation) {
        try {
            Map<String, Object> event = createBookingEvent(reservation, "BOOKING_COMPLETED");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_COMPLETED_KEY,
                    event
            );

            logger.info("✅ Event BOOKING_COMPLETED published for reservation {}", reservation.getId());
        } catch (Exception e) {
            logger.error("❌ Failed to publish BOOKING_COMPLETED event", e);
        }
    }

    /**
     * Créer un événement de réservation générique
     */
    private Map<String, Object> createBookingEvent(ReservationResponseDTO reservation, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("reservationId", reservation.getId());
        event.put("propertyId", reservation.getPropertyId());
        event.put("userId", reservation.getUserId());
        event.put("checkInDate", reservation.getCheckInDate());
        event.put("checkOutDate", reservation.getCheckOutDate());
        event.put("totalAmount", reservation.getPriceBreakdown().getTotalAmount());
        event.put("status", reservation.getStatus().name());
        event.put("timestamp", System.currentTimeMillis());

        if (reservation.getBlockchainTxHash() != null) {
            event.put("blockchainTxHash", reservation.getBlockchainTxHash());
        }

        return event;
    }
}