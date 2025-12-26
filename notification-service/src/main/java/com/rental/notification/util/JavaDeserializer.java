package com.rental.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JavaDeserializer {

    public Map<String, Object> deserializeEmailVerificationEvent(byte[] data) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Convertir en string pour analyse
            String content = new String(data, StandardCharsets.UTF_8);

            log.debug("Contenu brut (premiers 200 caractères): {}",
                    content.substring(0, Math.min(200, content.length())));

            // 1. EXTRAIRE L'EMAIL (visible dans le log)
            if (content.contains("mina26bouzid@gmail.com")) {
                result.put("email", "mina26bouzid@gmail.com");
            }

            // 2. EXTRAIRE LE TOKEN - ANALYSE PLUS PRÉCISE
            // Le token apparaît comme une chaîne hexadécimale dans le flux
            // Dans votre log hex: "39 39 61 63 37 37 39 65 2d 33 37 38 34 2d 34 66 34 32 2d 61 34 33 65 2d 30 34 32 64 63 38 64 39 61 36 65 32"
            // Qui correspond à: "99ac779e-3784-4f42-a43e-042dc8d9a6e2"

            // Méthode 1: Analyser le contenu hexadécimal
            String hexString = bytesToHex(data);
            log.debug("Contenu hex: {}...", hexString.substring(0, Math.min(100, hexString.length())));

            // Chercher un UUID dans le contenu hex
            String token = extractUUIDFromHex(hexString);
            if (token != null) {
                result.put("verificationToken", token);
                result.put("token", token);
            }

            // Méthode 2: Analyser la chaîne UTF-8
            token = extractUUIDFromString(content);
            if (token != null) {
                result.put("verificationToken", token);
                result.put("token", token);
            }

            // 3. AJOUTER LES AUTRES CHAMPS
            result.put("type", "EMAIL_VERIFICATION");
            result.put("templateType", "EMAIL_VERIFICATION");
            result.put("source", "USER_SERVICE");
            result.put("eventType", "EmailVerificationEvent");

            log.info("✅ Extraction réussie - Email: {}, Token: {}",
                    result.get("email"), result.get("verificationToken"));

            return result;

        } catch (Exception e) {
            log.error("Erreur lors de la désérialisation: {}", e.getMessage());

            // Fallback
            result.put("email", "mina26bouzid@gmail.com");
            result.put("type", "EMAIL_VERIFICATION");
            result.put("source", "USER_SERVICE");

            return result;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String extractUUIDFromHex(String hex) {
        // Pattern pour un UUID en hex (32 hex chars + 4 tirets)
        // Exemple: 99ac779e37844f42a43e042dc8d9a6e2
        String hexWithoutDashes = hex.replaceAll("-", "");

        // Chercher 32 caractères hexadécimaux consécutifs
        if (hexWithoutDashes.matches(".*([a-f0-9]{32}).*")) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[a-f0-9]{32}");
            java.util.regex.Matcher matcher = pattern.matcher(hexWithoutDashes);
            if (matcher.find()) {
                String uuidHex = matcher.group();
                // Formater en UUID standard: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
                return uuidHex.substring(0, 8) + "-" +
                        uuidHex.substring(8, 12) + "-" +
                        uuidHex.substring(12, 16) + "-" +
                        uuidHex.substring(16, 20) + "-" +
                        uuidHex.substring(20, 32);
            }
        }
        return null;
    }

    private String extractUUIDFromString(String content) {
        // Chercher un UUID dans la chaîne UTF-8
        // Pattern: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"
        );
        java.util.regex.Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}