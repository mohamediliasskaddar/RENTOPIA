package com.rentaldapp.bookingservice.controller;

import com.rentaldapp.bookingservice.model.dto.CreateBookingDTO;
import com.rentaldapp.bookingservice.model.dto.ReservationResponseDTO;
import com.rentaldapp.bookingservice.model.enums.ReservationStatus;
import com.rentaldapp.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/new")
    public ResponseEntity<ReservationResponseDTO> createBooking(
            @Valid @RequestBody CreateBookingDTO createBookingDTO,
            Authentication authentication) {
        Integer userId = extractUserIdFromAuthentication(authentication);
        ReservationResponseDTO reservation = bookingService.createBooking(createBookingDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable Integer id) {
        ReservationResponseDTO reservation = bookingService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/user/me")
    public ResponseEntity<List<ReservationResponseDTO>> getMyReservations(Authentication authentication) {
        Integer userId = extractUserIdFromAuthentication(authentication);
        List<ReservationResponseDTO> reservations = bookingService.getUserReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/user/upcoming")
    public ResponseEntity<List<ReservationResponseDTO>> getUpcomingReservations(Authentication authentication) {
        Integer userId = extractUserIdFromAuthentication(authentication);
        List<ReservationResponseDTO> reservations = bookingService.getUpcomingReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/user/past")
    public ResponseEntity<List<ReservationResponseDTO>> getPastReservations(Authentication authentication) {
        Integer userId = extractUserIdFromAuthentication(authentication);
        List<ReservationResponseDTO> reservations = bookingService.getPastReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReservationResponseDTO>> getPropertyReservations(@PathVariable Integer propertyId) {
        List<ReservationResponseDTO> reservations = bookingService.getPropertyReservations(propertyId);
        return ResponseEntity.ok(reservations);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirmReservation(
            @PathVariable Integer id,
            @RequestParam String blockchainTxHash) {
        ReservationResponseDTO reservation = bookingService.confirmReservation(id, blockchainTxHash);
        return ResponseEntity.ok(reservation);
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ReservationResponseDTO> checkIn(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer userId = extractUserIdFromAuthentication(authentication);
        ReservationResponseDTO reservation = bookingService.checkIn(id, userId);
        return ResponseEntity.ok(reservation);
    }

    @PatchMapping("/{id}/check-out")
    public ResponseEntity<ReservationResponseDTO> checkOut(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer userId = extractUserIdFromAuthentication(authentication);
        ReservationResponseDTO reservation = bookingService.checkOut(id, userId);
        return ResponseEntity.ok(reservation);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        Integer userId = extractUserIdFromAuthentication(authentication);
        ReservationResponseDTO reservation = bookingService.cancelReservation(id, userId, reason);
        return ResponseEntity.ok(reservation);
    }

    @PatchMapping("/{id}/release-escrow")
    public ResponseEntity<ReservationResponseDTO> releaseEscrow(
            @PathVariable Integer id,
            @RequestParam String txHash) {
        ReservationResponseDTO reservation = bookingService.releaseEscrow(id, txHash);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "booking-service");
        return ResponseEntity.ok(response);
    }

    // Méthode utilitaire pour extraire le userId de l'authentification
    private Integer extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("User not authenticated");
        }

        // Le principal est le userId en String (voir JwtAuthenticationFilter)
        String userIdStr = authentication.getName();
        try {
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            throw new SecurityException("Invalid user ID in authentication");
        }
    }







    /**
     * Récupérer toutes les réservations des propriétés du host connecté
     * GET /bookings/host/me
     */
    @GetMapping("/host/me")
    public ResponseEntity<List<ReservationResponseDTO>> getHostReservations(Authentication authentication) {
        Integer hostId = extractUserIdFromAuthentication(authentication);
        List<ReservationResponseDTO> reservations = bookingService.getHostReservations(hostId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Récupérer les réservations du host par statut
     * GET /bookings/host/me/status?status=CONFIRMED
     */
    @GetMapping("/host/me/status")
    public ResponseEntity<List<ReservationResponseDTO>> getHostReservationsByStatus(
            Authentication authentication,
            @RequestParam ReservationStatus status) {
        Integer hostId = extractUserIdFromAuthentication(authentication);
        List<ReservationResponseDTO> reservations = bookingService.getHostReservationsByStatus(hostId, status);
        return ResponseEntity.ok(reservations);
    }
}