package com.rental.media.util;

import com.rental.media.exception.InvalidImageFormatException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Classe utilitaire pour valider les images uploadées
 */
@Component
public class ImageValidator {

    @Value("${image.allowed-formats}")
    private String allowedFormatsStr;

    @Value("${image.max-size-mb}")
    private int maxSizeMb;

    /**
     * Valide qu'un fichier est une image valide
     */
    public void validateImage(MultipartFile file) {
        // Vérifier que le fichier n'est pas vide
        if (file == null || file.isEmpty()) {
            throw new InvalidImageFormatException("Le fichier est vide");
        }

        // Vérifier la taille
        long maxSizeBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidImageFormatException(
                    String.format("Le fichier dépasse la taille maximale de %d MB", maxSizeMb)
            );
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageFormatException("Le fichier n'est pas une image");
        }

        // Vérifier l'extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new InvalidImageFormatException("Nom de fichier manquant");
        }

        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedFormats = Arrays.asList(allowedFormatsStr.split(","));

        if (!allowedFormats.contains(extension)) {
            throw new InvalidImageFormatException(
                    String.format("Format non autorisé. Formats acceptés : %s", allowedFormatsStr)
            );
        }

        // Vérifier que c'est vraiment une image en essayant de la lire
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new InvalidImageFormatException("Le fichier n'est pas une image valide");
            }
        } catch (IOException e) {
            throw new InvalidImageFormatException("Impossible de lire l'image : " + e.getMessage());
        }
    }

    /**
     * Extrait l'extension d'un nom de fichier
     */
    public String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * Génère un nom de fichier sécurisé et unique
     */
    public String generateSecureFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = java.util.UUID.randomUUID().toString();
        return uuid + "." + extension;
    }
}