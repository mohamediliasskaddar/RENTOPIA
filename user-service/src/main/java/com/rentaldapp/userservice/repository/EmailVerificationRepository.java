package com.rentaldapp.userservice.repository;

import com.rentaldapp.userservice.model.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {

    Optional<EmailVerification> findByVerificationToken(String token);

    Optional<EmailVerification> findByUserIdAndIsUsedFalse(Integer userId);
}