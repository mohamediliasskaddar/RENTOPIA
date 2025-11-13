package com.rentaldapp.userservice.service;

import com.rentaldapp.userservice.exception.UserNotFoundException;
import com.rentaldapp.userservice.model.dto.UserLanguageDTO;
import com.rentaldapp.userservice.model.dto.UserResponseDTO;
import com.rentaldapp.userservice.model.entity.User;
import com.rentaldapp.userservice.repository.UserLanguageRepository;
import com.rentaldapp.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLanguageRepository userLanguageRepository;

    @Autowired
    private UserLanguageService userLanguageService;

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'email: " + email));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ NOUVEAU : Filtrer par langue
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByLanguage(String languageCode) {
        return userRepository.findByLanguageCode(languageCode).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO updateUser(Integer id, UserResponseDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        if (updateDTO.getNom() != null) {
            user.setNom(updateDTO.getNom());
        }
        if (updateDTO.getPrenom() != null) {
            user.setPrenom(updateDTO.getPrenom());
        }
        if (updateDTO.getTel() != null) {
            user.setTel(updateDTO.getTel());
        }
        if (updateDTO.getPhotoUrl() != null) {
            user.setPhotoUrl(updateDTO.getPhotoUrl());
        }
        if (updateDTO.getWalletAdresse() != null) {
            user.setWalletAdresse(updateDTO.getWalletAdresse());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponseDTO toggleHostRole(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

        user.setIsHost(!user.getIsHost());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
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

        // ✅ NOUVEAU : Charger les langues
        List<UserLanguageDTO> languages = userLanguageService.getUserLanguages(user.getId());
        dto.setLanguages(languages);

        return dto;
    }
}