package com.realestate.listing.entity;

import com.google.common.hash.Hashing;
import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "property_amenities_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyAmenitiesSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Integer snapshotId;

    @Column(name = "snapshot_hash", length = 64, unique = true)
    private String snapshotHash;

    @Column(columnDefinition = "JSON")
    private String amenitiesJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;




    @Transient // dire à Hibernate/JPA de NE PAS enregistrer dans BDD.
    // ObjectMapper vient de la bibliothèque Jackson.
    // Son rôle : convertir des objets Java en JSON et JSON en objets Java.
    // Ici, on crée un objet 'mapper' qui sera utilisé pour
    // 1. transformer un Set<Amenity> en JSON (pour le stocker dans la colonne amenitiesJson)
    // 2. transformer un JSON stocké dans amenitiesJson en Set<Amenity> quand on le lit
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();//outil pour convertir des objets Java en JSON et vice versa

    //transformer un Set<Amenity> en JSON
    public void setAmenities(Set<Amenity> amenities) {
        try {
            this.amenitiesJson = mapper.writeValueAsString(amenities);
        } catch (Exception e) {
            throw new RuntimeException("Erreur sérialisation amenities", e);
        }
    }

    //transformer un JSON stocké dans amenitiesJson en Set<Amenity>
    public Set<Amenity> getAmenities() {
        try {
            return mapper.readValue(this.amenitiesJson, mapper.getTypeFactory()
                    .constructCollectionType(HashSet.class, Amenity.class));
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    // avant insert
    @PrePersist
    private void generateHash() {  // generer le snapchot_hash
        if (this.snapshotHash == null) {
            String data = amenitiesJson != null ? amenitiesJson : "";
            this.snapshotHash = Hashing.sha256().hashString(data, StandardCharsets.UTF_8).toString();
        }
    }
}