package com.rental.review.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequest {

    @Size(min = 10, max = 2000, message = "L'avis doit contenir entre 10 et 2000 caractères")
    private String reviewText;

    private Boolean isVisible;
}