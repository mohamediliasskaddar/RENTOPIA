// src/main/java/com/realestate/listing/service/PropertyRulesSnapshotService.java
package com.realestate.listing.service;

import com.google.common.hash.Hashing;
import com.realestate.listing.entity.PropertyRule;
import com.realestate.listing.entity.PropertyRulesSnapshot;
import com.realestate.listing.entity.PropertyVersion;
import com.realestate.listing.repository.PropertyRulesSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyRulesSnapshotService {

    @Autowired
    private PropertyRulesSnapshotRepository snapshotRepository;

    // === CRUD ===
    public List<PropertyRulesSnapshot> getAllSnapshots() {
        return snapshotRepository.findAll();
    }

    public Optional<PropertyRulesSnapshot> getSnapshotById(Integer id) {
        return snapshotRepository.findById(id);
    }

    public PropertyRulesSnapshot createSnapshot(PropertyRulesSnapshot snapshot) {
        // Optionnel : vérifier si le hash existe déjà (déduplication)
        // Mais pas obligatoire ici car on peut avoir plusieurs versions
        return snapshotRepository.save(snapshot);
    }

    public PropertyRulesSnapshot updateSnapshot(Integer id, PropertyRulesSnapshot updatedSnapshot) {
        PropertyRulesSnapshot existing = snapshotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot de règles non trouvé avec l'ID : " + id));

        updatedSnapshot.setSnapshotId(id);
        return snapshotRepository.save(updatedSnapshot);
    }

    public void deleteSnapshot(Integer id) {
        if (!snapshotRepository.existsById(id)) {
            throw new IllegalArgumentException("Snapshot de règles non trouvé avec l'ID : " + id);
        }
        snapshotRepository.deleteById(id);
    }







    public PropertyRulesSnapshot getOrCreateSnapshot(PropertyRule rules, PropertyVersion version) {
        if (rules == null) return createEmptySnapshot();

        String data = String.format("%s%s%s%s%s%s",
                rules.getChildrenAllowed() != null ? rules.getChildrenAllowed() : "",
                rules.getBabiesAllowed() != null ? rules.getBabiesAllowed() : "",
                rules.getPetsAllowed() != null ? rules.getPetsAllowed() : "",
                rules.getSmokingAllowed() != null ? rules.getSmokingAllowed() : "",
                rules.getEventsAllowed() != null ? rules.getEventsAllowed() : "",
                rules.getCustomRules() != null ? rules.getCustomRules() : ""
        );

        String hash = Hashing.sha256().hashString(data, StandardCharsets.UTF_8).toString();

        return snapshotRepository.findBySnapshotHash(hash)
                .orElseGet(() -> {
                    PropertyRulesSnapshot snapshot = new PropertyRulesSnapshot();
                    snapshot.setChildrenAllowed(rules.getChildrenAllowed());
                    snapshot.setBabiesAllowed(rules.getBabiesAllowed());
                    snapshot.setPetsAllowed(rules.getPetsAllowed());
                    snapshot.setSmokingAllowed(rules.getSmokingAllowed());
                    snapshot.setEventsAllowed(rules.getEventsAllowed());
                    snapshot.setCustomRules(rules.getCustomRules());
                    snapshot.setSnapshotHash(hash);
                    snapshot.setCreatedAt(LocalDateTime.now());
                    return snapshotRepository.save(snapshot);
                });
    }


    public PropertyRulesSnapshot createEmptySnapshot() {
        String emptyData = "falsefalsefalsefalsefalse";
        String hash = Hashing.sha256().hashString(emptyData, StandardCharsets.UTF_8).toString();

        return snapshotRepository.findBySnapshotHash(hash)
                .orElseGet(() -> {
                    PropertyRulesSnapshot snapshot = new PropertyRulesSnapshot();
                    snapshot.setChildrenAllowed(false);
                    snapshot.setBabiesAllowed(false);
                    snapshot.setPetsAllowed(false);
                    snapshot.setSmokingAllowed(false);
                    snapshot.setEventsAllowed(false);
                    snapshot.setCustomRules("");
                    snapshot.setSnapshotHash(hash);
                    snapshot.setCreatedAt(LocalDateTime.now());
                    return snapshotRepository.save(snapshot);
                });
    }
}