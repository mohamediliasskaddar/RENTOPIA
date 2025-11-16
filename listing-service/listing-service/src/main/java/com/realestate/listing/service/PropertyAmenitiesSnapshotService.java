// src/main/java/com/realestate/listing/service/PropertyAmenitiesSnapshotService.java
package com.realestate.listing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.realestate.listing.entity.Amenity;
import com.realestate.listing.entity.PropertyAmenitiesSnapshot;
import com.realestate.listing.entity.PropertyVersion;
import com.realestate.listing.repository.PropertyAmenitiesSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class PropertyAmenitiesSnapshotService {

    @Autowired
    private PropertyAmenitiesSnapshotRepository snapshotRepository;

    @Autowired
    private ObjectMapper objectMapper;


    // === CRUD ===
    public List<PropertyAmenitiesSnapshot> getAllSnapshots() {
        return snapshotRepository.findAll();
    }

    public Optional<PropertyAmenitiesSnapshot> getSnapshotById(Integer id) {
        return snapshotRepository.findById(id);
    }

    public PropertyAmenitiesSnapshot createSnapshot(PropertyAmenitiesSnapshot snapshot) {
        return snapshotRepository.save(snapshot);
    }

    public PropertyAmenitiesSnapshot updateSnapshot(Integer id, PropertyAmenitiesSnapshot updatedSnapshot) {
        PropertyAmenitiesSnapshot existing = snapshotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot non trouvé avec l'ID : " + id));

        updatedSnapshot.setSnapshotId(id);
        return snapshotRepository.save(updatedSnapshot);
    }

    public void deleteSnapshot(Integer id) {
        if (!snapshotRepository.existsById(id)) {
            throw new IllegalArgumentException("Snapshot non trouvé avec l'ID : " + id);
        }
        snapshotRepository.deleteById(id);
    }



    private final ObjectMapper mapper = new ObjectMapper();
    public PropertyAmenitiesSnapshot getOrCreateSnapshot(Set<Amenity> amenities, PropertyVersion version) {
        if (amenities == null || amenities.isEmpty()) return createEmptySnapshot();;


        try {
            // Sérialise SEULEMENT les champs nécessaires
            String json = objectMapper.writeValueAsString(
                    amenities.stream()
                            .map(a -> new AmenityDTO(a.getAmenityId(), a.getName(), a.getCategory(), a.getIcone()))
                            .toList()
            );

            String hash = Hashing.sha256()
                    .hashString(json, StandardCharsets.UTF_8)
                    .toString();

            return snapshotRepository.findBySnapshotHash(hash)
                    .orElseGet(() -> {
                        PropertyAmenitiesSnapshot snapshot = new PropertyAmenitiesSnapshot();
                        snapshot.setAmenitiesJson(json);
                        snapshot.setSnapshotHash(hash);
                        snapshot.setCreatedAt(LocalDateTime.now());
                        return snapshotRepository.save(snapshot);
                    });

        } catch (Exception e) {
            throw new RuntimeException("Erreur snapshot amenities", e);
        }
    }

    public PropertyAmenitiesSnapshot createEmptySnapshot() {
        String emptyJson = "[]";
        String hash = Hashing.sha256().hashString(emptyJson, StandardCharsets.UTF_8).toString();

        return snapshotRepository.findBySnapshotHash(hash)
                .orElseGet(() -> {
                    PropertyAmenitiesSnapshot snapshot = new PropertyAmenitiesSnapshot();
                    snapshot.setAmenitiesJson(emptyJson);
                    snapshot.setSnapshotHash(hash);
                    snapshot.setCreatedAt(LocalDateTime.now());
                    return snapshotRepository.save(snapshot);
                });
    }

}
// DTO pour éviter la boucle
record AmenityDTO(Integer amenityId, String name, String category, String icone) {}