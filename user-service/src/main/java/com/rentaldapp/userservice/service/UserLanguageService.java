package com.rentaldapp.userservice.service;

import com.rentaldapp.userservice.model.dto.AddLanguageDTO;
import com.rentaldapp.userservice.model.dto.UserLanguageDTO;
import com.rentaldapp.userservice.model.entity.Language;
import com.rentaldapp.userservice.model.entity.UserLanguage;
import com.rentaldapp.userservice.repository.LanguageRepository;
import com.rentaldapp.userservice.repository.UserLanguageRepository;
import com.rentaldapp.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserLanguageService {

    @Autowired
    private UserLanguageRepository userLanguageRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserLanguageDTO> getUserLanguages(Integer userId) {
        return userLanguageRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserLanguageDTO addLanguageToUser(Integer userId, AddLanguageDTO addLanguageDTO) {
        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId);
        }

        // Vérifier que la langue existe
        Language language = languageRepository.findById(addLanguageDTO.getLanguageId())
                .orElseThrow(() -> new RuntimeException("Langue non trouvée avec l'ID: " + addLanguageDTO.getLanguageId()));

        // Vérifier si l'utilisateur a déjà cette langue
        if (userLanguageRepository.existsByUserIdAndLanguageId(userId, addLanguageDTO.getLanguageId())) {
            throw new RuntimeException("L'utilisateur parle déjà cette langue");
        }

        // Créer l'association
        UserLanguage userLanguage = new UserLanguage();
        userLanguage.setUserId(userId);
        userLanguage.setLanguageId(addLanguageDTO.getLanguageId());
        userLanguage.setProficiencyLevel(addLanguageDTO.getProficiencyLevel());

        UserLanguage saved = userLanguageRepository.save(userLanguage);
        return convertToDTO(saved);
    }

    @Transactional
    public void removeLanguageFromUser(Integer userId, Integer languageId) {
        if (!userLanguageRepository.existsByUserIdAndLanguageId(userId, languageId)) {
            throw new RuntimeException("L'utilisateur ne parle pas cette langue");
        }
        userLanguageRepository.deleteByUserIdAndLanguageId(userId, languageId);
    }

    @Transactional
    public UserLanguageDTO updateUserLanguageProficiency(Integer userId, Integer languageId,
                                                         UserLanguage.ProficiencyLevel newLevel) {
        UserLanguage userLanguage = userLanguageRepository.findByUserIdAndLanguageId(userId, languageId)
                .orElseThrow(() -> new RuntimeException("Association non trouvée"));

        userLanguage.setProficiencyLevel(newLevel);
        UserLanguage updated = userLanguageRepository.save(userLanguage);
        return convertToDTO(updated);
    }

    private UserLanguageDTO convertToDTO(UserLanguage userLanguage) {
        UserLanguageDTO dto = new UserLanguageDTO();
        dto.setLanguageId(userLanguage.getLanguageId());
        dto.setProficiencyLevel(userLanguage.getProficiencyLevel());

        if (userLanguage.getLanguage() != null) {
            dto.setLanguageCode(userLanguage.getLanguage().getCode());
            dto.setLanguageName(userLanguage.getLanguage().getName());
            dto.setLanguageNativeName(userLanguage.getLanguage().getNativeName());
        }

        return dto;
    }
}