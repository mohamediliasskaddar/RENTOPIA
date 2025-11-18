package com.rentaldapp.userservice.service;

import com.rentaldapp.userservice.client.NotificationServiceClient;
import com.rentaldapp.userservice.model.entity.EmailVerification;
import com.rentaldapp.userservice.model.entity.User;
import com.rentaldapp.userservice.repository.EmailVerificationRepository;
import com.rentaldapp.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private NotificationServiceClient notificationClient;

    @Transactional
    public void sendEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Désactiver les anciennes vérifications
        Optional<EmailVerification> existing = emailVerificationRepository.findByUserIdAndIsUsedFalse(user.getId());
        existing.ifPresent(verification -> {
            verification.setIsUsed(true);
            emailVerificationRepository.save(verification);
        });

        // Créer nouvelle vérification
        EmailVerification verification = new EmailVerification();
        verification.setUserId(user.getId());
        verification.setVerificationToken(UUID.randomUUID().toString());
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setIsUsed(false);

        emailVerificationRepository.save(verification);

        // Appel au Notification Service
        notificationClient.sendVerificationEmail(email, verification.getVerificationToken());
    }

    @Transactional
    public void confirmEmailVerification(String token) {
        EmailVerification verification = emailVerificationRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (verification.getIsUsed()) {
            throw new RuntimeException("Token déjà utilisé");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        // Marquer comme vérifié
        verification.setIsUsed(true);
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        // Mettre à jour l'user
        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }
}