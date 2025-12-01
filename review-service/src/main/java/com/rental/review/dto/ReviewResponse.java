package com.rental.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Integer reservationId;
    private Integer userId;
    private Integer propertyId;
    private String reviewText;
    private Double ratingValue;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String guestName;  // Optionnel
    private String propertyTitle;  // Optionnel
}