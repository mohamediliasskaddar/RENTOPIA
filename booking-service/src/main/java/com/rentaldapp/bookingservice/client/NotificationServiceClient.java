package com.rentaldapp.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client Feign pour communiquer avec le Notification Service
 */
@FeignClient(name = "notification-service", url = "${services.user.url:http://localhost:8086}")
public interface NotificationServiceClient {

    /**
     * Envoyer une notification de confirmation de réservation
     */
    @PostMapping("/notifications/booking-confirmed")
    void sendBookingConfirmation(
            @RequestParam("userId") Integer userId,
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("email") String email
    );

    /**
     * Envoyer une notification d'annulation
     */
    @PostMapping("/notifications/booking-cancelled")
    void sendBookingCancellation(
            @RequestParam("userId") Integer userId,
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("email") String email,
            @RequestParam("reason") String reason
    );

    /**
     * Rappel de check-in
     */
    @PostMapping("/notifications/checkin-reminder")
    void sendCheckInReminder(
            @RequestParam("userId") Integer userId,
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("email") String email
    );

    /**
     * Notification de check-in effectué
     */
    @PostMapping("/notifications/checkin-completed")
    void sendCheckInCompleted(
            @RequestParam("userId") Integer userId,
            @RequestParam("reservationId") Integer reservationId
    );

    /**
     * Notification de check-out effectué
     */
    @PostMapping("/notifications/checkout-completed")
    void sendCheckOutCompleted(
            @RequestParam("userId") Integer userId,
            @RequestParam("reservationId") Integer reservationId
    );
}