package com.rental.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReviewStats {

    private Integer userId;
    private Long totalReviewsGiven;
    private Long totalReviewsReceived;
    private Double averageRatingReceived;
}