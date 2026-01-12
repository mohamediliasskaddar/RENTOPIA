// src/main/java/com/realestate/listing/entity/PropertyGeneralSnapshot.java
package com.realestate.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_general_snapshot")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyGeneralSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer snapshotId;

    @Column(name = "snapshot_hash", unique = true, nullable = false)
    private String snapshotHash;

    @Column(name = "general_json", columnDefinition = "TEXT")
    private String generalJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}