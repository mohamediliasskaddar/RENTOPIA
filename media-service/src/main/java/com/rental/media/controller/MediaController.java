package com.rental.media.controller;

import com.rental.media.dto.ImageUploadRequest;
import com.rental.media.dto.ImageUploadResponse;
import com.rental.media.service.MediaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour les opérations sur les médias
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @Value("${app.mode:local}")
    private String appMode;

    /**
     * Upload d'une image de propriété
     */
    @PostMapping(
            value = "/properties/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImageUploadResponse> uploadPropertyImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityId") Integer entityId,
            @RequestParam(value = "isCover", required = false, defaultValue = "false") Boolean isCover,
            @RequestParam(value = "displayOrder", required = false, defaultValue = "0") Integer displayOrder
    ) {
        log.info("📤 Upload image pour propriété {} (mode: {})", entityId, appMode);

        try {
            ImageUploadRequest request = new ImageUploadRequest(
                    "PROPERTY",
                    entityId,
                    isCover,
                    displayOrder
            );

            ImageUploadResponse response = mediaService.uploadPropertyImage(file, request);

            log.info("✅ Upload réussi : {}", response.getCdnUrl());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("❌ Erreur upload : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Upload d'une photo de profil utilisateur
     */
    @PostMapping(
            value = "/users/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImageUploadResponse> uploadUserPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId
    ) {
        log.info("📤 Upload photo profil pour utilisateur {} (mode: {})", userId, appMode);

        try {
            ImageUploadResponse response = mediaService.uploadUserPhoto(file, userId);

            log.info("✅ Photo profil mise à jour : {}", response.getCdnUrl());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("❌ Erreur upload : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Suppression d'une photo de propriété
     */
    @DeleteMapping("/properties/{photoId}")
    public ResponseEntity<Map<String, String>> deletePropertyPhoto(
            @PathVariable Integer photoId
    ) {
        log.info("🗑️ Suppression photo {} (mode: {})", photoId, appMode);

        try {
            mediaService.deletePropertyPhoto(photoId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Photo supprimée avec succès");
            response.put("photoId", photoId.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur suppression : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Servir les fichiers en mode local (développement uniquement)
     * Permet d'afficher les images uploadées localement
     *
     * Exemple d'URL : GET http://localhost:8087/api/media/files/properties/1/abc-123.jpg
     */
    @GetMapping("/files/**")
    public ResponseEntity<byte[]> serveLocalFile(HttpServletRequest request) {

        // Vérifier que le mode est "local"
        if (!"local".equalsIgnoreCase(appMode)) {
            log.warn("⚠️ Tentative d'accès aux fichiers locaux en mode production");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Endpoint disponible uniquement en mode local".getBytes());
        }

        try {
            // Extraire le chemin complet depuis l'URI
            // Ex: /api/media/files/properties/1/abc-123.jpg → properties/1/abc-123.jpg
            String fullPath = request.getRequestURI();
            String key = fullPath.replace("/api/media/files/", "");

            log.debug("📂 Lecture du fichier : {}", key);

            // Lire le fichier depuis le système de fichiers local
            byte[] fileData = mediaService.getLocalFile(key);

            // Déterminer le Content-Type selon l'extension
            String contentType = determineContentType(key);

            log.info("✅ Fichier servi : {} ({} bytes)", key, fileData.length);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileData);

        } catch (IOException e) {
            log.error("❌ Fichier non trouvé : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(("Fichier non trouvé : " + e.getMessage()).getBytes());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la lecture du fichier : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur serveur : " + e.getMessage()).getBytes());
        }
    }

    /**
     * Détermine le Content-Type selon l'extension du fichier
     */
    private String determineContentType(String filename) {
        if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "image/jpeg"; // Par défaut
        }
    }

    /**
     * Health check avec info sur le mode
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "media-service");
        health.put("mode", appMode);
        health.put("storage", appMode.equals("local") ? "Filesystem local" : "AWS S3 (IAM Roles)");
        return ResponseEntity.ok(health);
    }
}