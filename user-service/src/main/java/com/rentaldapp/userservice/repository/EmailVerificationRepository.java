package com.rentaldapp.userservice.repository;

import com.rentaldapp.userservice.model.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {

    Optional<EmailVerification> findByVerificationToken(String token);

    Optional<EmailVerification> findByUserIdAndIsUsedFalse(Integer userId);
    // AJOUTEZ CETTE MÉTHODE
    @Query(value = "SELECT * FROM email_verifications WHERE verification_token = ?1", nativeQuery = true)
    Optional<EmailVerification> findByTokenNative(String token);

    // Pour debug: toutes les entrées
    @Query(value = "SELECT verification_token FROM email_verifications", nativeQuery = true)
    List<String> findAllTokens();
}