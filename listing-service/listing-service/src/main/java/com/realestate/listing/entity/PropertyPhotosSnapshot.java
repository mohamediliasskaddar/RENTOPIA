package com.realestate.listing.entity;

import com.google.common.hash.Hashing;
import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "property_photos_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyPhotosSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Integer snapshotId;


    @Column(columnDefinition = "JSON")
    private String photosJson;  // Stocke un tableau JSON

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "snapshot_hash", length = 64, unique = true)
    private String snapshotHash;


    // ObjectMapper vient de la bibliothèque Jackson.
    // Son rôle : convertir des objets Java en JSON et JSON en objets Java.
    @Transient
    private final ObjectMapper mapper = new ObjectMapper();


    //transformer un Set<PropertyPhoto> en JSON
    public void setPhotosAsList(java.util.List<PropertyPhoto> photos) {
        try {
            this.photosJson = mapper.writeValueAsString(photos);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sérialisation des photos", e);
        }
    }

    //transformer un JSON stocké dans photosJson en List<PropertyPhoto>
    public java.util.List<PropertyPhoto> getPhotosAsList() {
        try {
            return mapper.readValue(this.photosJson, mapper.getTypeFactory()
                    .constructCollectionType(java.util.List.class, PropertyPhoto.class));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la désérialisation des photos", e);
        }
    }


    // avant insert
    @PrePersist
    private void generateHash() {  // generer le snapchot_hash
        if (this.snapshotHash == null) {
            String data = photosJson != null ? photosJson : "";
            this.snapshotHash = Hashing.sha256().hashString(data, StandardCharsets.UTF_8).toString();
        }
    }
}