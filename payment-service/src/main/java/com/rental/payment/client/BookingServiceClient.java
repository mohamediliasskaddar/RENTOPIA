package com.rental.payment.client;

import com.rental.payment.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "booking-service",
        configuration = FeignConfig.class,
        fallback = BookingServiceClientFallback.class
)
public interface BookingServiceClient {

    @GetMapping("/bookings/{reservationId}")
    Map<String, Object> getBookingById(  // ❌ ENLEVEZ ResponseEntity
                                         @PathVariable("reservationId") Integer reservationId
    );

    @PatchMapping("/bookings/{reservationId}/confirm")
    Map<String, Object> confirmBooking(  // ❌ ENLEVEZ ResponseEntity
                                         @PathVariable("reservationId") Integer reservationId,
                                         @RequestParam("blockchainTxHash") String txHash
    );

    @PatchMapping("/bookings/{reservationId}/release-escrow")
    Map<String, Object> releaseBookingEscrow(  // ❌ ENLEVEZ ResponseEntity
                                               @PathVariable("reservationId") Integer reservationId,
                                               @RequestParam("txHash") String txHash
    );

    @GetMapping("/bookings/health")
    Map<String, Object> healthCheck();  // ❌ ENLEVEZ ResponseEntity
}