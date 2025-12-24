// src/main/java/com/realestate/listing/service/HostInteractionPreferenceService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.HostInteractionPreference;
import com.realestate.listing.repository.HostInteractionPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HostInteractionPreferenceService {

    @Autowired
    private HostInteractionPreferenceRepository preferenceRepository;

    // === CRUD ===
    public List<HostInteractionPreference> getAllPreferences() {
        return preferenceRepository.findAll();
    }

    public Optional<HostInteractionPreference> getPreferenceById(Integer id) {
        return preferenceRepository.findById(id);
    }

    public HostInteractionPreference createPreference(HostInteractionPreference preference) {
        // Vérifier si une préférence existe déjà pour cette propriété
        Integer propertyId = preference.getProperty().getPropertyId();
        if (preferenceRepository.findByProperty_PropertyId(propertyId).isPresent()) {
            throw new IllegalArgumentException("Une préférence existe déjà pour cette propriété (ID: " + propertyId + ")");
        }
        return preferenceRepository.save(preference);
    }

    public HostInteractionPreference updatePreference(Integer id, HostInteractionPreference updatedPreference) {
        HostInteractionPreference existing = preferenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Préférence non trouvée avec l'ID : " + id));

        updatedPreference.setPreferenceId(id);
        return preferenceRepository.save(updatedPreference);
    }

    public void deletePreference(Integer id) {
        if (!preferenceRepository.existsById(id)) {
            throw new IllegalArgumentException("Préférence non trouvée avec l'ID : " + id);
        }
        preferenceRepository.deleteById(id);
    }

    // === RECHERCHE PAR PROPRIÉTÉ ===
    public Optional<HostInteractionPreference> getByPropertyId(Integer propertyId) {
        return preferenceRepository.findByProperty_PropertyId(propertyId);
    }
}