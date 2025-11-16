package com.realestate.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_version")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Integer versionId;

    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "num_version")
    private Integer numVersion;


    // === RELATIONS SNAPSHOTS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amenities_snapshot_id", nullable = false)
    private PropertyAmenitiesSnapshot amenitiesSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rules_snapshot_id", nullable = false)
    private PropertyRulesSnapshot rulesSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photos_snapshot_id", nullable = false)
    private PropertyPhotosSnapshot photosSnapshot;

    @ManyToOne
    @JoinColumn(name = "general_snapshot_id", nullable = false)
    private PropertyGeneralSnapshot generalSnapshot;
}