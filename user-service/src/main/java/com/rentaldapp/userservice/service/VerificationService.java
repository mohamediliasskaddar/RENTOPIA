package com.rentaldapp.userservice.service;

import com.rentaldapp.userservice.event.EmailVerificationEvent;
import com.rentaldapp.userservice.event.producer.NotificationEventProducer;
import com.rentaldapp.userservice.model.entity.EmailVerification;
import com.rentaldapp.userservice.model.entity.User;
import com.rentaldapp.userservice.repository.EmailVerificationRepository;
import com.rentaldapp.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;  // AJOUTER CET IMPORT

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class VerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private NotificationEventProducer notificationEventProducer;

    @Transactional
    public void sendEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

     /*   // Désactiver les anciennes vérifications
        Optional<EmailVerification> existing = emailVerificationRepository.findByUserIdAndIsUsedFalse(user.getId());
        existing.ifPresent(verification -> {
            verification.setIsUsed(true);
            emailVerificationRepository.save(verification);
        }); */

        // Créer nouvelle vérification
        EmailVerification verification = new EmailVerification();
        verification.setUserId(user.getId());
        verification.setVerificationToken(UUID.randomUUID().toString());
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setIsUsed(false);

        EmailVerification savedVerification = emailVerificationRepository.save(verification);

        // ENVOI ASYNCHRONE via RabbitMQ - CORRECTION ICI
        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setEmail(email);
        event.setVerificationToken(savedVerification.getVerificationToken());
        event.setTemplateType("EMAIL_VERIFICATION");
        // timestamp sera automatiquement défini dans le constructeur

        notificationEventProducer.sendEmailVerificationEvent(event);
    }

    @Transactional
    public void confirmEmailVerification(String token) {

        // LOG IMPORTANT: Afficher exactement ce qui est reçu
        log.info("🎯 ========== DÉBUT VÉRIFICATION TOKEN ==========");
        log.info("🔑 Token reçu: '{}'", token);
        log.info("📏 Longueur du token: {}", token.length());
        log.info("🔍 Pattern UUID valide: {}",
                token.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));

        // Nettoyer le token (au cas où)
        String cleanedToken = token.trim();
        log.info("🧹 Token nettoyé: '{}'", cleanedToken);

        // Essayer plusieurs méthodes de recherche
        Optional<EmailVerification> verification = Optional.empty();

        // Méthode 1: JPA standard
        verification = emailVerificationRepository.findByVerificationToken(cleanedToken);
        log.info("🔍 Méthode 1 (JPA standard): {}", verification.isPresent() ? "TROUVÉ" : "NON TROUVÉ");

        // Méthode 2: Requête native (ignore la casse)
        if (verification.isEmpty()) {
            String nativeSql = "SELECT * FROM email_verifications WHERE verification_token = ?";
            // Implémentez cette méthode dans le repository
            verification = emailVerificationRepository.findByTokenNative(cleanedToken);
            log.info("🔍 Méthode 2 (Native SQL): {}", verification.isPresent() ? "TROUVÉ" : "NON TROUVÉ");
        }

        // Méthode 3: Recherche insensible à la casse
        if (verification.isEmpty()) {
            String lowerToken = cleanedToken.toLowerCase();
            List<EmailVerification> allTokens = emailVerificationRepository.findAll();
            for (EmailVerification ev : allTokens) {
                if (ev.getVerificationToken().toLowerCase().equals(lowerToken)) {
                    verification = Optional.of(ev);
                    log.info("🔍 Méthode 3 (Comparaison manuelle): TROUVÉ");
                    break;
                }
            }
        }

        if (verification.isEmpty()) {
            log.error("❌ TOKEN ABSOLUMENT INTROUVABLE dans la base");
            log.info("📋 Liste de tous les tokens en base:");
            emailVerificationRepository.findAll().forEach(ev -> {
                log.info("   - Token: '{}' (length: {})", ev.getVerificationToken(), ev.getVerificationToken().length());
            });
            throw new RuntimeException("Token invalide");
        }

        EmailVerification ev = verification.get();
        log.info("✅ TOKEN TROUVÉ! Détails:");
        log.info("   ID: {}", ev.getId());
        log.info("   User ID: {}", ev.getUserId());
        log.info("   Is Used: {}", ev.getIsUsed());
        log.info("   Expires At: {}", ev.getExpiresAt());
        log.info("   Verified At: {}", ev.getVerifiedAt());

    }}