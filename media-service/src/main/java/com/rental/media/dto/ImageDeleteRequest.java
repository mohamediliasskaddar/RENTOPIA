package com.rental.media.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour supprimer une image
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDeleteRequest {

    @NotNull(message = "L'ID de la photo est requis")
    private Integer photoId;

    private String entityType; // "PROPERTY" ou "USER" (optionnel pour validation)
}