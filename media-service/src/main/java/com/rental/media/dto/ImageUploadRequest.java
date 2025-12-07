package com.rental.media.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la requête d'upload d'image
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {

    @NotNull(message = "Le type d'entité est requis")
    private String entityType; // "PROPERTY" ou "USER"

    @NotNull(message = "L'ID de l'entité est requis")
    private Integer entityId; // ID de la propriété ou de l'utilisateur

    private Boolean isCover; // true si c'est la photo de couverture

    private Integer displayOrder; // ordre d'affichage (pour les galeries)
}