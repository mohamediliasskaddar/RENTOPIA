// src/main/java/com/realestate/listing/dto/PropertyVersionDTO.java
package com.realestate.listing.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyVersionDTO {
    private Integer versionId;
    private Integer propertyId;
    private Integer numVersion;
    private LocalDateTime createdAt;

    private GeneralSnapshotDTO generalSnapshot;
    private AmenitiesSnapshotDTO amenitiesSnapshot;
    private PhotosSnapshotDTO photosSnapshot;
    private RulesSnapshotDTO rulesSnapshot;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneralSnapshotDTO {
        private Integer snapshotId;
        private String snapshotHash;
        private String generalJson;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AmenitiesSnapshotDTO {
        private Integer snapshotId;
        private String snapshotHash;
        private String amenitiesJson;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PhotosSnapshotDTO {
        private Integer snapshotId;
        private String snapshotHash;
        private String photosJson;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RulesSnapshotDTO {
        private Integer snapshotId;
        private String snapshotHash;
        private Boolean childrenAllowed;
        private Boolean babiesAllowed;
        private Boolean petsAllowed;
        private Boolean smokingAllowed;
        private Boolean eventsAllowed;
        private String customRules;
        private LocalDateTime createdAt;
    }
}