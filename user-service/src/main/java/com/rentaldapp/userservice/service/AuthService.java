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
    private JwtTokenProvider jwtTokenProvider; // ✅ Changé de JwtService à JwtTokenProvider

    @Transactional
    public JwtResponseDTO register(RegisterDTO registerDTO) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }

        // Vérifier si le wallet existe déjà (si fourni)
        if (registerDTO.getWalletAdresse() != null &&
                !registerDTO.getWalletAdresse().isEmpty() &&
                userRepository.existsByWalletAdresse(registerDTO.getWalletAdresse())) {
            throw new RuntimeException("Cette adresse wallet est déjà utilisée");
        }

        // Créer un nouvel utilisateur
        User user = new User();
        user.setNom(registerDTO.getNom());
        user.setPrenom(registerDTO.getPrenom());
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setTel(registerDTO.getTel());
        user.setWalletAdresse(registerDTO.getWalletAdresse());
        user.setEmailVerified(false);
        user.setTelephoneVerified(false);
        user.setIsGuest(true);
        user.setIsHost(false);

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Générer le token JWT
        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getId());

        // Créer la réponse
        UserResponseDTO userResponse = convertToDTO(savedUser);

        return new JwtResponseDTO(token, userResponse);
    }

    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginDTO loginDTO) {
        // Trouver l'utilisateur par email
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou mot de passe incorrect"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email ou mot de passe incorrect");
        }

        // Générer le token JWT
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());

        // Créer la réponse
        UserResponseDTO userResponse = convertToDTO(user);

        return new JwtResponseDTO(token, userResponse);
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