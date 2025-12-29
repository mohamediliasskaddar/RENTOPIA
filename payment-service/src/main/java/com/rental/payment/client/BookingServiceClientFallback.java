package com.rental.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BookingServiceClientFallback implements BookingServiceClient {

    @Override
    public Map<String, Object> getBookingById(Integer reservationId) {  // ❌ ENLEVEZ ResponseEntity
        log.warn("⚠️ Fallback: Booking Service unavailable for getBookingById - reservation #{}", reservationId);

        // Retournez directement Map<String, Object> au lieu de ResponseEntity
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", reservationId);
        mockData.put("status", "PENDING");
        mockData.put("userId", 1);
        mockData.put("propertyId", 1);
        mockData.put("totalAmount", 182.0);
        mockData.put("tenantWalletAddress", "0xMockTenantAddress");
        mockData.put("hostWalletAddress", "0xMockHostAddress");
        mockData.put("message", "Fallback: Using mock data");
        mockData.put("tenantId", 1);  // AJOUTEZ tenantId pour getUserIdFromReservation()

        return mockData;  // ❌ Retournez directement la Map
    }

    @Override
    public Map<String, Object> confirmBooking(Integer reservationId, String txHash) {  // ❌ ENLEVEZ ResponseEntity
        log.warn("⚠️ Fallback: Cannot confirm booking #{}. Transaction hash: {}", reservationId, txHash);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Fallback: Booking confirmation skipped");
        response.put("reservationId", reservationId);
        response.put("txHash", txHash);
        response.put("status", "PENDING_CONFIRMATION");

        return response;  // ❌ Retournez directement la Map
    }

    @Override
    public Map<String, Object> releaseBookingEscrow(Integer reservationId, String txHash) {  // ❌ ENLEVEZ ResponseEntity
        log.warn("⚠️ Fallback: Cannot release escrow for booking #{}. Transaction hash: {}", reservationId, txHash);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Fallback: Escrow release skipped");
        response.put("reservationId", reservationId);
        response.put("txHash", txHash);

        return response;  // ❌ Retournez directement la Map
    }

    @Override
    public Map<String, Object> healthCheck() {  // ❌ ENLEVEZ ResponseEntity
        log.warn("⚠️ Fallback: Booking Service health check unavailable");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "FALLBACK");
        response.put("service", "booking-service");
        response.put("message", "Service temporarily unavailable - using fallback");

        return response;  // ❌ Retournez directement la Map
    }
}