package com.realestate.listing.entity;

import com.google.common.hash.Hashing;
import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_rules_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyRulesSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Integer snapshotId;

    @Column(name = "snapshot_hash", length = 64, unique = true)
    private String snapshotHash;


    private Boolean childrenAllowed;
    private Boolean babiesAllowed;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private Boolean eventsAllowed;
    private String customRules;


    @Column(name = "created_at")
    private LocalDateTime createdAt;



    @PrePersist
    private void generateHash() {
        if (this.snapshotHash == null) {
            String data = String.format("%s%s%s%s%s%s",
                    childrenAllowed != null ? childrenAllowed : "",
                    babiesAllowed != null ? babiesAllowed : "",
                    petsAllowed != null ? petsAllowed : "",
                    smokingAllowed != null ? smokingAllowed : "",
                    eventsAllowed != null ? eventsAllowed : "",
                    customRules != null ? customRules : ""
            );
            this.snapshotHash = Hashing.sha256()
                    .hashString(data, StandardCharsets.UTF_8)
                    .toString();
        }
    }
}