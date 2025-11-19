package com.rentaldapp.userservice.service;

import com.rentaldapp.userservice.exception.InvalidCredentialsException;
import com.rentaldapp.userservice.model.dto.JwtResponseDTO;
import com.rentaldapp.userservice.model.dto.LoginDTO;
import com.rentaldapp.userservice.model.dto.RegisterDTO;
import com.rentaldapp.userservice.model.dto.UserResponseDTO;
import com.rentaldapp.userservice.model.entity.User;
import com.rentaldapp.userservice.repository.UserRepository;
import com.rentaldapp.userservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private VerificationService verificationService; // AJOUT

    @Transactional
    public JwtResponseDTO register(RegisterDTO registerDTO) {
        // 1. Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }

        // 2. Vérifier si le wallet existe déjà
        if (userRepository.existsByWalletAdresse(registerDTO.getWalletAdresse())) {
            throw new RuntimeException("Cette adresse wallet est déjà utilisée");
        }

        // 3. Créer user avec email/password ET wallet
        User user = new User();
        user.setNom(registerDTO.getNom());
        user.setPrenom(registerDTO.getPrenom());
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setWalletAdresse(registerDTO.getWalletAdresse());
        user.setTel(registerDTO.getTel());
        user.setEmailVerified(false);
        user.setTelephoneVerified(false);
        user.setIsGuest(true);
        user.setIsHost(false);

        User savedUser = userRepository.save(user);

        // 4. ENVOI AUTOMATIQUE EMAIL DE VÉRIFICATION
        try {
            verificationService.sendEmailVerification(savedUser.getEmail());
        } catch (Exception e) {
            System.err.println("Erreur envoi email verification: " + e.getMessage());
        }

        // 5. Générer JWT avec wallet comme subject
        String token = jwtTokenProvider.generateToken(savedUser.getWalletAdresse(), savedUser.getId());
        UserResponseDTO userResponse = convertToDTO(savedUser);

        return new JwtResponseDTO(token, userResponse);
    }

    // CORRIGER le nom de la méthode
    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginDTO loginDTO) {
        // 1. Vérifier la signature
        if (!verifySignature(loginDTO.getWalletAdresse(), loginDTO.getSignature())) {
            throw new InvalidCredentialsException("Signature invalide");
        }

        // 2. Trouver user par wallet
        User user = userRepository.findByWalletAdresse(loginDTO.getWalletAdresse())
                .orElseThrow(() -> new InvalidCredentialsException("Wallet non enregistré"));

        // 3. Générer JWT avec wallet comme subject
        String token = jwtTokenProvider.generateToken(user.getWalletAdresse(), user.getId());
        UserResponseDTO userResponse = convertToDTO(user);

        return new JwtResponseDTO(token, userResponse);
    }

    private boolean verifySignature(String walletAddress, String signature) {
        // À IMPLÉMENTER
        return true; // Temporaire
    }

    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setTel(user.getTel());
        dto.setWalletAdresse(user.getWalletAdresse());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setTelephoneVerified(user.getTelephoneVerified());
        dto.setIsGuest(user.getIsGuest());
        dto.setIsHost(user.getIsHost());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}