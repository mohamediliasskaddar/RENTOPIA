package com.rental.media.service;

import com.rental.media.dto.ImageUploadRequest;
import com.rental.media.dto.ImageUploadResponse;
import com.rental.media.exception.ImageUploadException;
import com.rental.media.util.ImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Service principal pour la gestion des médias
 *
 * MODE LOCAL : Stockage filesystem (dossier local/uploads/)
 * MODE PRODUCTION : Stockage S3 avec IAM Roles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {

    private final S3Client s3Client;
    private final ImageValidator imageValidator;
    private final ImageCompressionService compressionService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${aws.s3.properties-bucket}")
    private String propertiesBucket;

    @Value("${aws.s3.users-bucket}")
    private String usersBucket;

    @Value("${aws.s3.cdn-url}")
    private String cdnUrl;

    @Value("${app.mode:local}")
    private String appMode;

    // Dossier local pour mode développement
    private static final String LOCAL_STORAGE_PATH = "local-uploads";

    /**
     * Upload une image pour une propriété
     */
    @Transactional
    public ImageUploadResponse uploadPropertyImage(
            MultipartFile file,
            ImageUploadRequest request
    ) throws IOException {
        // 1. Validation
        imageValidator.validateImage(file);

        // 2. Compression
        byte[] compressedImage = compressionService.compressImage(file);

        // 3. Générer miniature
        byte[] thumbnail = compressionService.generateThumbnail(compressedImage);

        // 4. Calculer hash
        String imageHash = calculateHash(compressedImage);

        // 5. Générer clé S3 unique
        String fileName = imageValidator.generateSecureFileName(file.getOriginalFilename());
        String s3Key = String.format("properties/%d/%s", request.getEntityId(), fileName);
        String thumbnailKey = String.format("properties/%d/thumbnails/%s",
                request.getEntityId(), fileName);

        // 6. Upload (S3 ou local selon le mode)
        if ("local".equalsIgnoreCase(appMode)) {
            uploadToLocalStorage(s3Key, compressedImage);
            uploadToLocalStorage(thumbnailKey, thumbnail);
        } else {
            uploadToS3(propertiesBucket, s3Key, compressedImage, file.getContentType());
            uploadToS3(propertiesBucket, thumbnailKey, thumbnail, file.getContentType());
        }

        // 7. Récupérer dimensions
        int[] dimensions = compressionService.getImageDimensions(compressedImage);

        // 8. Sauvegarder en base de données
        Integer photoId = savePropertyPhotoToDatabase(
                request.getEntityId(),
                s3Key,
                imageHash,
                request.getIsCover(),
                request.getDisplayOrder()
        );

        // 9. Construire l'URL
        String imageUrl = buildImageUrl(s3Key);
        String thumbnailUrl = buildImageUrl(thumbnailKey);

        // 10. Retourner la réponse
        return ImageUploadResponse.builder()
                .photoId(photoId)
                .s3Key(s3Key)
                .cdnUrl(imageUrl)
                .thumbnailUrl(thumbnailUrl)
                .fileSize((long) compressedImage.length)
                .contentType(file.getContentType())
                .width(dimensions[0])
                .height(dimensions[1])
                .isCover(request.getIsCover())
                .displayOrder(request.getDisplayOrder())
                .message("Image uploadée avec succès")
                .build();
    }

    /**
     * Upload une photo de profil utilisateur
     */
    @Transactional
    public ImageUploadResponse uploadUserPhoto(
            MultipartFile file,
            Integer userId
    ) throws IOException {
        // 1. Validation
        imageValidator.validateImage(file);

        // 2. Compression
        byte[] compressedImage = compressionService.compressImage(file);

        // 3. Générer clé S3
        String fileName = imageValidator.generateSecureFileName(file.getOriginalFilename());
        String s3Key = String.format("users/%d/%s", userId, fileName);

        // 4. Upload
        if ("local".equalsIgnoreCase(appMode)) {
            uploadToLocalStorage(s3Key, compressedImage);
        } else {
            uploadToS3(usersBucket, s3Key, compressedImage, file.getContentType());
        }

        // 5. Mettre à jour la table user
        String photoUrl = buildImageUrl(s3Key);
        String updateSql = "UPDATE user SET photo_url = ? WHERE id = ?";
        jdbcTemplate.update(updateSql, photoUrl, userId);

        // 6. Retourner la réponse
        return ImageUploadResponse.builder()
                .s3Key(s3Key)
                .cdnUrl(photoUrl)
                .fileSize((long) compressedImage.length)
                .contentType(file.getContentType())
                .message("Photo de profil mise à jour avec succès")
                .build();
    }

    /**
     * Upload vers S3 (PRODUCTION - Utilise IAM Roles automatiquement)
     */
    private void uploadToS3(String bucket, String key, byte[] data, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));

            log.info("✅ Fichier uploadé sur S3 : {}/{}", bucket, key);

        } catch (S3Exception e) {
            log.error("❌ Erreur upload S3 : {}", e.getMessage());
            throw new ImageUploadException("Erreur lors de l'upload vers S3 : " + e.getMessage());
        }
    }

    /**
     * Upload vers filesystem local (DÉVELOPPEMENT)
     */
    private void uploadToLocalStorage(String key, byte[] data) {
        try {
            // Créer le chemin complet
            Path filePath = Paths.get(LOCAL_STORAGE_PATH, key);

            // Créer les dossiers si nécessaires
            Files.createDirectories(filePath.getParent());

            // Écrire le fichier
            Files.write(filePath, data);

            log.info("💾 Fichier sauvegardé localement : {}", filePath);

        } catch (IOException e) {
            log.error("❌ Erreur stockage local : {}", e.getMessage());
            throw new ImageUploadException("Erreur lors du stockage local : " + e.getMessage());
        }
    }

    /**
     * Construire l'URL de l'image selon le mode
     */
    private String buildImageUrl(String key) {
        if ("local".equalsIgnoreCase(appMode)) {
            // Mode local : URL vers le serveur local
            return "http://localhost:8087/api/media/files/" + key;
        } else {
            // Mode production : URL CloudFront
            return cdnUrl + "/" + key;
        }
    }

    /**
     * Sauvegarde les métadonnées en base
     */
    private Integer savePropertyPhotoToDatabase(
            Integer propertyId,
            String s3Key,
            String hash,
            Boolean isCover,
            Integer displayOrder
    ) {
        String sql = """
            INSERT INTO property_photos 
            (property_id, photo_url, photo_hash, is_cover, display_order) 
            VALUES (?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql, propertyId, s3Key, hash,
                isCover != null ? isCover : false,
                displayOrder != null ? displayOrder : 0);

        String getIdSql = "SELECT LAST_INSERT_ID()";
        return jdbcTemplate.queryForObject(getIdSql, Integer.class);
    }

    /**
     * Calcule le hash SHA-256 d'une image
     */
    private String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur calcul hash", e);
        }
    }

    /**
     * Supprime une photo de propriété
     */
    @Transactional
    public void deletePropertyPhoto(Integer photoId) {
        // 1. Récupérer la clé S3
        String sql = "SELECT photo_url FROM property_photos WHERE photo_id = ?";
        String s3Key = jdbcTemplate.queryForObject(sql, String.class, photoId);

        if (s3Key == null) {
            throw new ImageUploadException("Photo non trouvée");
        }

        // 2. Supprimer fichier
        if ("local".equalsIgnoreCase(appMode)) {
            deleteFromLocalStorage(s3Key);
        } else {
            deleteFromS3(propertiesBucket, s3Key);
        }

        // 3. Supprimer de la base
        String deleteSql = "DELETE FROM property_photos WHERE photo_id = ?";
        jdbcTemplate.update(deleteSql, photoId);

        log.info("✅ Photo {} supprimée avec succès", photoId);
    }

    /**
     * Supprime de S3 (PRODUCTION)
     */
    private void deleteFromS3(String bucket, String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("✅ Fichier supprimé de S3 : {}/{}", bucket, key);

        } catch (S3Exception e) {
            log.warn("⚠️ Erreur suppression S3 : {}", e.getMessage());
        }
    }

    /**
     * Supprime du filesystem local (DÉVELOPPEMENT)
     */
    private void deleteFromLocalStorage(String key) {
        try {
            Path filePath = Paths.get(LOCAL_STORAGE_PATH, key);
            Files.deleteIfExists(filePath);
            log.info("💾 Fichier supprimé localement : {}", filePath);

        } catch (IOException e) {
            log.warn("⚠️ Erreur suppression locale : {}", e.getMessage());
        }
    }

    /**
     * Récupère un fichier local (pour servir les images en mode développement)
     */
    public byte[] getLocalFile(String key) throws IOException {
        Path filePath = Paths.get(LOCAL_STORAGE_PATH, key);

        if (!Files.exists(filePath)) {
            throw new ImageUploadException("Fichier non trouvé : " + key);
        }

        return Files.readAllBytes(filePath);
    }
}