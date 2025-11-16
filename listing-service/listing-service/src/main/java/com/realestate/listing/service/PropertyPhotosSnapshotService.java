// src/main/java/com/realestate/listing/service/PropertyPhotosSnapshotService.java
package com.realestate.listing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.realestate.listing.entity.PropertyPhoto;
import com.realestate.listing.entity.PropertyPhotosSnapshot;
import com.realestate.listing.entity.PropertyVersion;
import com.realestate.listing.repository.PropertyPhotosSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyPhotosSnapshotService {

    @Autowired
    private PropertyPhotosSnapshotRepository snapshotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public PropertyPhotosSnapshot createSnapshot(PropertyPhotosSnapshot snapshot) {
        return snapshotRepository.save(snapshot);
    }



    public List<PropertyPhotosSnapshot> getAll() {
        return snapshotRepository.findAll();
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public PropertyPhotosSnapshot getOrCreateSnapshot(List<PropertyPhoto> photos, PropertyVersion version) {
        if (photos == null || photos.isEmpty()) return createEmptySnapshot();;

        try {
            String json = objectMapper.writeValueAsString(
                    photos.stream()
                            .map(p -> new PhotoDTO(p.getPhotoUrl(), p.getIsCover(), p.getDisplayOrder()))
                            .toList()
            );

            String hash = Hashing.sha256()
                    .hashString(json, StandardCharsets.UTF_8)
                    .toString();

            return snapshotRepository.findBySnapshotHash(hash)
                    .orElseGet(() -> {
                        PropertyPhotosSnapshot snapshot = new PropertyPhotosSnapshot();
                        snapshot.setPhotosJson(json);
                        snapshot.setSnapshotHash(hash);
                        snapshot.setCreatedAt(LocalDateTime.now());
                        return snapshotRepository.save(snapshot);
                    });

        } catch (Exception e) {
            throw new RuntimeException("Erreur snapshot photos", e);
}
        }

    public PropertyPhotosSnapshot createEmptySnapshot() {
        String emptyJson = "[]";
        String hash = Hashing.sha256().hashString(emptyJson, StandardCharsets.UTF_8).toString();

        return snapshotRepository.findBySnapshotHash(hash)
                .orElseGet(() -> {
                    PropertyPhotosSnapshot snapshot = new PropertyPhotosSnapshot();
                    snapshot.setPhotosJson(emptyJson);
                    snapshot.setSnapshotHash(hash);
                    snapshot.setCreatedAt(LocalDateTime.now());
                    return snapshotRepository.save(snapshot);
                });
    }

        }




record PhotoDTO(String photoUrl, Boolean isCover, Integer displayOrder) {}