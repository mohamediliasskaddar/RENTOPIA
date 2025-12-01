package com.rental.review.service;

import com.rental.review.dto.RatingRequest;
import com.rental.review.entity.Rating;
import com.rental.review.entity.Review;
import com.rental.review.exception.ReviewException;
import com.rental.review.exception.ReviewNotFoundException;
import com.rental.review.repository.RatingRepository;
import com.rental.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Ajouter/Mettre à jour une note pour un avis existant
     */
    @Transactional
    public Rating updateRating(Long reviewId, Double ratingValue) {
        log.info("Mise à jour de la note pour l'avis: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Avis non trouvé"));

        Rating rating = review.getRating();

        if (rating == null) {
            // Créer une nouvelle note
            rating = Rating.builder()
                    .reservationId(review.getReservationId())
                    .propertyId(review.getPropertyId())
                    .review(review)
                    .ratingValue(ratingValue)
                    .build();
        } else {
            // Mettre à jour la note existante
            rating.setRatingValue(ratingValue);
        }

        Rating savedRating = ratingRepository.save(rating);

        log.info("Note mise à jour avec succès pour l'avis: {}", reviewId);

        return savedRating;
    }

    /**
     * Calculer la note moyenne pour une propriété
     */
    public Double getAverageRatingForProperty(Integer propertyId) {
        log.info("Calcul de la note moyenne pour la propriété: {}", propertyId);

        Double average = ratingRepository.calculateAverageRatingForProperty(propertyId);

        if (average == null) {
            return 0.0;
        }

        // Arrondir à 1 décimale
        return Math.round(average * 10.0) / 10.0;
    }
}