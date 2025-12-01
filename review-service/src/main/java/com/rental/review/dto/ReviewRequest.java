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
public class ReviewRequest {

    @NotNull(message = "Reservation ID est obligatoire")
    private Integer reservationId;

    @NotNull(message = "User ID est obligatoire")
    private Integer userId;

    @NotNull(message = "Property ID est obligatoire")
    private Integer propertyId;

    @NotBlank(message = "Le texte de l'avis est obligatoire")
    @Size(min = 10, max = 2000, message = "L'avis doit contenir entre 10 et 2000 caractères")
    private String reviewText;

    @NotNull(message = "La note est obligatoire")
    @DecimalMin(value = "1.0", message = "La note minimale est 1.0")
    @DecimalMax(value = "5.0", message = "La note maximale est 5.0")
    private Double ratingValue;
}