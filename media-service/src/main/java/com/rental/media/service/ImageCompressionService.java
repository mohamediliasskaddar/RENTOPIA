package com.rental.media.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service pour compresser et redimensionner les images
 */
@Service
public class ImageCompressionService {

    @Value("${image.compression.quality}")
    private double compressionQuality;

    @Value("${image.compression.max-width}")
    private int maxWidth;

    @Value("${image.compression.max-height}")
    private int maxHeight;

    @Value("${image.thumbnail.width}")
    private int thumbnailWidth;

    @Value("${image.thumbnail.height}")
    private int thumbnailHeight;

    /**
     * Compresse une image sans changer ses dimensions (ou en la réduisant si trop grande)
     */
    public byte[] compressImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        if (originalImage == null) {
            throw new IOException("Impossible de lire l'image");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Déterminer le format de sortie (jpg par défaut)
        String outputFormat = determineOutputFormat(file.getOriginalFilename());

        Thumbnails.of(originalImage)
                .size(maxWidth, maxHeight) // Réduire si plus grand que max
                .outputQuality(compressionQuality) // 0.85 = 85% qualité
                .outputFormat(outputFormat) // ✅ FORMAT SPÉCIFIÉ
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Génère une miniature (thumbnail) de l'image
     */
    public byte[] generateThumbnail(byte[] imageBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("Impossible de lire l'image pour la miniature");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(originalImage)
                .size(thumbnailWidth, thumbnailHeight) // 400x300 par défaut
                .outputQuality(0.75) // Qualité réduite pour miniature
                .outputFormat("jpg") // ✅ FORMAT SPÉCIFIÉ (JPEG pour miniatures)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Récupère les dimensions d'une image
     */
    public int[] getImageDimensions(byte[] imageBytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (image == null) {
            throw new IOException("Impossible de lire les dimensions de l'image");
        }

        return new int[]{image.getWidth(), image.getHeight()};
    }

    /**
     * Détermine le format de sortie selon le nom du fichier
     */
    private String determineOutputFormat(String filename) {
        if (filename == null) {
            return "jpg";
        }

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".png")) {
            return "png";
        } else if (lowerFilename.endsWith(".webp")) {
            return "webp";
        } else if (lowerFilename.endsWith(".gif")) {
            return "gif";
        } else {
            return "jpg"; // Par défaut JPEG
        }
    }
}