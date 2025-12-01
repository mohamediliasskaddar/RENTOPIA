package com.rental.review.service;

import com.rental.review.dto.*;
import com.rental.review.entity.Rating;
import com.rental.review.entity.Review;
import com.rental.review.exception.ReviewException;
import com.rental.review.exception.ReviewNotFoundException;
import com.rental.review.repository.RatingRepository;
import com.rental.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RatingRepository ratingRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${review.edit-window-hours:24}")
    private int editWindowHours;

    /**
     * Créer un nouvel avis avec note
     */
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        log.info("Création d'un avis pour la réservation: {}", request.getReservationId());

        // Vérifier qu'un avis n'existe pas déjà
        if (reviewRepository.existsByReservationId(request.getReservationId())) {
            throw new ReviewException("Un avis existe déjà pour cette réservation");
        }

        // Créer le review
        Review review = Review.builder()
                .reservationId(request.getReservationId())
                .propertyId(request.getPropertyId())
                .reviewText(request.getReviewText())
                .isVisible(true)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Créer la note associée
        Rating rating = Rating.builder()
                .reservationId(request.getReservationId())
                .propertyId(request.getPropertyId())
                .review(savedReview)
                .ratingValue(request.getRatingValue())
                .build();

        ratingRepository.save(rating);
        savedReview.setRating(rating);

        // Publier un événement RabbitMQ
        publishReviewCreatedEvent(request.getUserId(), request.getPropertyId(),
                request.getReservationId(), request.getRatingValue());

        log.info("Avis créé avec succès: {}", savedReview.getId());

        return mapToResponse(savedReview, request.getUserId());
    }

    /**
     * Récupérer un avis par ID
     */
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Avis non trouvé"));

        return mapToResponse(review, null);
    }

    /**
     * Récupérer un avis par reservation ID
     */
    public ReviewResponse getReviewByReservationId(Integer reservationId) {
        Review review = reviewRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ReviewNotFoundException("Aucun avis pour cette réservation"));

        return mapToResponse(review, null);
    }

    /**
     * Récupérer tous les avis d'une propriété
     */
    public List<ReviewResponse> getPropertyReviews(Integer propertyId) {
        log.info("Récupération des avis pour la propriété: {}", propertyId);

        List<Review> reviews = reviewRepository.findByPropertyIdAndIsVisibleTrueOrderByCreatedAtDesc(propertyId);

        return reviews.stream()
                .map(review -> mapToResponse(review, null))
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les avis donnés par un utilisateur
     */
    public List<ReviewResponse> getUserReviews(Integer userId) {
        log.info("Récupération des avis donnés par l'utilisateur: {}", userId);

        List<Review> reviews = reviewRepository.findByUserId(userId);

        return reviews.stream()
                .map(review -> mapToResponse(review, userId))
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour un avis
     */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, Integer userId) {
        log.info("Mise à jour de l'avis: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Avis non trouvé"));

        // Vérifier la fenêtre de modification (24h par défaut)
        LocalDateTime editDeadline = review.getCreatedAt().plusHours(editWindowHours);
        if (LocalDateTime.now().isAfter(editDeadline)) {
            throw new ReviewException("La période de modification est expirée");
        }

        // Mettre à jour le texte si fourni
        if (request.getReviewText() != null && !request.getReviewText().isBlank()) {
            review.setReviewText(request.getReviewText());
        }

        // Mettre à jour la visibilité si fournie
        if (request.getIsVisible() != null) {
            review.setIsVisible(request.getIsVisible());
        }

        Review updatedReview = reviewRepository.save(review);

        log.info("Avis mis à jour avec succès: {}", reviewId);

        return mapToResponse(updatedReview, userId);
    }

    /**
     * Supprimer un avis (soft delete en le rendant invisible)
     */
    @Transactional
    public void deleteReview(Long reviewId, Integer userId) {
        log.info("Suppression de l'avis: {} par l'utilisateur: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Avis non trouvé"));

        review.setIsVisible(false);
        reviewRepository.save(review);

        log.info("Avis supprimé (rendu invisible) avec succès: {}", reviewId);
    }

    /**
     * Supprimer définitivement un avis
     */
    @Transactional
    public void hardDeleteReview(Long reviewId) {
        log.info("Suppression définitive de l'avis: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Avis non trouvé"));

        reviewRepository.delete(review);

        log.info("Avis supprimé définitivement: {}", reviewId);
    }

    /**
     * Obtenir les statistiques des avis pour une propriété
     */
    public PropertyReviewStats getPropertyStats(Integer propertyId) {
        log.info("Calcul des statistiques pour la propriété: {}", propertyId);

        Long totalReviews = reviewRepository.countByPropertyIdAndIsVisibleTrue(propertyId);
        Double averageRating = ratingRepository.calculateAverageRatingForProperty(propertyId);

        // Compter par étoiles
        Long fiveStar = ratingRepository.countByPropertyIdAndRatingRange(propertyId, 4.5, 5.1);
        Long fourStar = ratingRepository.countByPropertyIdAndRatingRange(propertyId, 3.5, 4.5);
        Long threeStar = ratingRepository.countByPropertyIdAndRatingRange(propertyId, 2.5, 3.5);
        Long twoStar = ratingRepository.countByPropertyIdAndRatingRange(propertyId, 1.5, 2.5);
        Long oneStar = ratingRepository.countByPropertyIdAndRatingRange(propertyId, 0.0, 1.5);

        return PropertyReviewStats.builder()
                .propertyId(propertyId)
                .totalReviews(totalReviews)
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .fiveStarCount(fiveStar)
                .fourStarCount(fourStar)
                .threeStarCount(threeStar)
                .twoStarCount(twoStar)
                .oneStarCount(oneStar)
                .build();
    }

    /**
     * Publier un événement RabbitMQ lors de la création d'un avis
     */
    private void publishReviewCreatedEvent(Integer userId, Integer propertyId,
                                           Integer reservationId, Double rating) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("propertyId", propertyId);
            event.put("reservationId", reservationId);
            event.put("rating", rating);
            event.put("eventType", "REVIEW_CREATED");
            event.put("timestamp", LocalDateTime.now().toString());

            rabbitTemplate.convertAndSend(
                    "review.exchange",
                    "review.created",
                    event);

            log.info("Événement REVIEW_CREATED publié pour la réservation: {}", reservationId);

        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement: {}", e.getMessage());
        }
    }

    /**
     * Mapper Review entity vers ReviewResponse DTO
     */
    private ReviewResponse mapToResponse(Review review, Integer userId) {
        return ReviewResponse.builder()
                .id(review.getId())
                .reservationId(review.getReservationId())
                .userId(userId)
                .propertyId(review.getPropertyId())
                .reviewText(review.getReviewText())
                .ratingValue(review.getRating() != null ? review.getRating().getRatingValue() : null)
                .isVisible(review.getIsVisible())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}