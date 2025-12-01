package com.rental.review.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {

    @NotNull(message = "Reservation ID est obligatoire")
    private Integer reservationId;

    @NotNull(message = "La note est obligatoire")
    @DecimalMin(value = "1.0", message = "La note minimale est 1.0")
    @DecimalMax(value = "5.0", message = "La note maximale est 5.0")
    private Double ratingValue;
}