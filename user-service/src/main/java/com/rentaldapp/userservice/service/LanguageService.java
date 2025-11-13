package com.rentaldapp.userservice.service;

import com.rentaldapp.userservice.model.dto.LanguageDTO;
import com.rentaldapp.userservice.model.entity.Language;
import com.rentaldapp.userservice.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LanguageService {

    @Autowired
    private LanguageRepository languageRepository;

    @Transactional(readOnly = true)
    public List<LanguageDTO> getAllLanguages() {
        return languageRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LanguageDTO getLanguageById(Integer id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Langue non trouvée avec l'ID: " + id));
        return convertToDTO(language);
    }

    @Transactional(readOnly = true)
    public LanguageDTO getLanguageByCode(String code) {
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Langue non trouvée avec le code: " + code));
        return convertToDTO(language);
    }

    private LanguageDTO convertToDTO(Language language) {
        return new LanguageDTO(
                language.getId(),
                language.getCode(),
                language.getName(),
                language.getNativeName()
        );
    }
}