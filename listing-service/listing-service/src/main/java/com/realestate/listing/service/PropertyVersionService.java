//src/main/java/com/realestate/listing/service/PropertyVersionService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.*;
import com.realestate.listing.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class PropertyVersionService {

    @Autowired
    private PropertyVersionRepository versionRepository;
    @Autowired
    private PropertyAmenitiesSnapshotService amenitiesSnapshotService;

    @Autowired
    private PropertyRulesSnapshotService rulesSnapshotService;

    @Autowired
    private PropertyPhotosSnapshotService photosSnapshotService;
    @Autowired
    private PropertyPhotosSnapshotRepository photosSnapshotRepository;

    @Autowired
    private PropertyGeneralSnapshotService generalSnapshotService;
    // === CRUD ===
    public List<PropertyVersion> getAllVersions() {
        return versionRepository.findAll();
    }

    public Optional<PropertyVersion> getVersionById(Integer id) {
        return versionRepository.findById(id);
    }

    public PropertyVersion createVersion(PropertyVersion version) {
        // Optionnel : valider que numVersion est unique par propertyId
        if (version.getNumVersion() != null &&
                versionRepository.findByPropertyIdAndNumVersion(
                        version.getPropertyId(), version.getNumVersion()).isPresent()) {
            throw new IllegalArgumentException(
                    "La version " + version.getNumVersion() +
                            " existe déjà pour la propriété ID " + version.getPropertyId());
        }
        return versionRepository.save(version);
    }


    @Transactional
    public PropertyVersion createVersionFromProperty(Property property, int numVersion) {
        PropertyVersion version = PropertyVersion.builder()
                .propertyId(property.getPropertyId())
                .numVersion(numVersion)
                .createdAt(LocalDateTime.now())
                .build();

        version.setGeneralSnapshot(generalSnapshotService.getOrCreateSnapshot(property));

        // === AMENITIES : TOUJOURS un snapshot (même si vide) ===
        PropertyAmenitiesSnapshot amenitiesSnapshot = amenitiesSnapshotService.getOrCreateSnapshot(property.getAmenities() , version);
        if (amenitiesSnapshot == null) {
            // Cas extrême : amenities = null ou vide → snapshot vide
            amenitiesSnapshot = amenitiesSnapshotService.createEmptySnapshot();
        }
        version.setAmenitiesSnapshot(amenitiesSnapshot);

        // === PHOTOS : TOUJOURS un snapshot ===
        List<PropertyPhoto> photos = new ArrayList<>(property.getPhotos());
        PropertyPhotosSnapshot photosSnapshot = photosSnapshotService.getOrCreateSnapshot(photos , version);
        if (photosSnapshot == null) {
            photosSnapshot = photosSnapshotService.createEmptySnapshot();
        }
        version.setPhotosSnapshot(photosSnapshot);

        // === RULES : TOUJOURS un snapshot ===
        PropertyRulesSnapshot rulesSnapshot = rulesSnapshotService.getOrCreateSnapshot(property.getRules() , version);
        if (rulesSnapshot == null) {
            rulesSnapshot = rulesSnapshotService.createEmptySnapshot();
        }
        version.setRulesSnapshot(rulesSnapshot);

        return versionRepository.save(version);
    }

    public PropertyVersion updateVersion(Integer id, PropertyVersion updatedVersion) {
        PropertyVersion existing = versionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Version non trouvée avec l'ID : " + id));

        updatedVersion.setVersionId(id);
        return versionRepository.save(updatedVersion);
    }

    public void deleteVersion(Integer id) {
        if (!versionRepository.existsById(id)) {
            throw new IllegalArgumentException("Version non trouvée avec l'ID : " + id);
        }
        versionRepository.deleteById(id);
    }

    // === RECHERCHES PAR PROPRIÉTÉ ===

    /** Toutes les versions d'une propriété (du plus récent au plus ancien) */
    public List<PropertyVersion> getAllByPropertyId(Integer propertyId) {
        return versionRepository.findByPropertyIdOrderByNumVersionDesc(propertyId);
    }

    /** Une version spécifique */
    public Optional<PropertyVersion> getByPropertyIdAndVersion(
            Integer propertyId, Integer numVersion) {
        return versionRepository.findByPropertyIdAndNumVersion(propertyId, numVersion);
    }

    /** Version actuelle (la plus récente) */
    public Optional<PropertyVersion> getCurrentVersion(Integer propertyId) {
        return versionRepository.findTopByPropertyIdOrderByNumVersionDesc(propertyId);
    }
}