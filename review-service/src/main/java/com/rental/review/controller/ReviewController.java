package com.rental.review.controller;

import com.rental.review.dto.*;
import com.rental.review.entity.Rating;
import com.rental.review.service.RatingService;
import com.rental.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final RatingService ratingService;

    /**
     * Health check
     * GET http://localhost:8087/api/reviews/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Review Service",
                "port", "8087"
        ));
    }

    /**
     * Créer un nouvel avis
     * POST http://localhost:8087/api/reviews
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("Requête de création d'avis pour la réservation: {}", request.getReservationId());
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer un avis par ID
     * GET http://localhost:8087/api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        log.info("Récupération de l'avis: {}", id);
        ReviewResponse response = reviewService.getReviewById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un avis par reservation ID
     * GET http://localhost:8087/api/reviews/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ReviewResponse> getReviewByReservationId(@PathVariable Integer reservationId) {
        log.info("Récupération de l'avis pour la réservation: {}", reservationId);
        ReviewResponse response = reviewService.getReviewByReservationId(reservationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les avis d'une propriété
     * GET http://localhost:8087/api/reviews/property/{propertyId}
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponse>> getPropertyReviews(@PathVariable Integer propertyId) {
        log.info("Récupération des avis pour la propriété: {}", propertyId);
        List<ReviewResponse> reviews = reviewService.getPropertyReviews(propertyId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Récupérer tous les avis donnés par un utilisateur
     * GET http://localhost:8087/api/reviews/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Integer userId) {
        log.info("Récupération des avis de l'utilisateur: {}", userId);
        List<ReviewResponse> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Mettre à jour un avis
     * PUT http://localhost:8087/api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request,
            @RequestParam Integer userId) {
        log.info("Mise à jour de l'avis: {} par l'utilisateur: {}", id, userId);
        ReviewResponse response = reviewService.updateReview(id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un avis (soft delete)
     * DELETE http://localhost:8087/api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        log.info("Suppression de l'avis: {} par l'utilisateur: {}", id, userId);
        reviewService.deleteReview(id, userId);
        return ResponseEntity.ok(Map.of("message", "Avis supprimé avec succès"));
    }

    /**
     * Obtenir les statistiques d'une propriété
     * GET http://localhost:8087/api/reviews/property/{propertyId}/stats
     */
    @GetMapping("/property/{propertyId}/stats")
    public ResponseEntity<PropertyReviewStats> getPropertyStats(@PathVariable Integer propertyId) {
        log.info("Récupération des statistiques pour la propriété: {}", propertyId);
        PropertyReviewStats stats = reviewService.getPropertyStats(propertyId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtenir la note moyenne d'une propriété
     * GET http://localhost:8087/api/reviews/property/{propertyId}/average-rating
     */
    @GetMapping("/property/{propertyId}/average-rating")
    public ResponseEntity<Map<String, Double>> getAverageRating(@PathVariable Integer propertyId) {
        log.info("Calcul de la note moyenne pour la propriété: {}", propertyId);
        Double average = ratingService.getAverageRatingForProperty(propertyId);
        return ResponseEntity.ok(Map.of("averageRating", average));
    }

    /**
     * Mettre à jour la note d'un avis
     * PUT http://localhost:8087/api/reviews/{reviewId}/rating
     */
    @PutMapping("/{reviewId}/rating")
    public ResponseEntity<Rating> updateRating(
            @PathVariable Long reviewId,
            @RequestParam Double ratingValue) {
        log.info("Mise à jour de la note pour l'avis: {}", reviewId);
        Rating rating = ratingService.updateRating(reviewId, ratingValue);
        return ResponseEntity.ok(rating);
    }
}
