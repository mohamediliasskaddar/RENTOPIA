package com.rental.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse après upload d'image
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {

    private Integer photoId; // ID généré en base de données

    private String s3Key; // Clé dans S3 (ex: properties/123/abc-def.jpg)

    private String cdnUrl; // URL complète via CloudFront

    private String thumbnailUrl; // URL de la miniature (si générée)

    private Long fileSize; // Taille en bytes

    private String contentType; // image/jpeg, image/png, etc.

    private Integer width; // Largeur de l'image

    private Integer height; // Hauteur de l'image

    private Boolean isCover; // true si photo de couverture

    private Integer displayOrder; // ordre d'affichage

    private String message; // message de succès
}