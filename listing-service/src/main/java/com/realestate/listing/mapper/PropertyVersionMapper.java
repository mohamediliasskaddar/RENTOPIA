// src/main/java/com/realestate/listing/mapper/PropertyVersionMapper.java
package com.realestate.listing.mapper;

import com.realestate.listing.dto.PropertyVersionDTO;
import com.realestate.listing.entity.*;
import org.springframework.stereotype.Component;

@Component
public class PropertyVersionMapper {

    public PropertyVersionDTO toDTO(PropertyVersion version) {
        if (version == null) return null;

        return PropertyVersionDTO.builder()
                .versionId(version.getVersionId())
                .propertyId(version.getPropertyId())
                .numVersion(version.getNumVersion())
                .createdAt(version.getCreatedAt())
                .generalSnapshot(toGeneralDTO(version.getGeneralSnapshot()))
                .amenitiesSnapshot(toAmenitiesDTO(version.getAmenitiesSnapshot()))
                .photosSnapshot(toPhotosDTO(version.getPhotosSnapshot()))
                .rulesSnapshot(toRulesDTO(version.getRulesSnapshot()))
                .build();
    }

    private PropertyVersionDTO.GeneralSnapshotDTO toGeneralDTO(PropertyGeneralSnapshot snapshot) {
        if (snapshot == null) return null;
        return PropertyVersionDTO.GeneralSnapshotDTO.builder()
                .snapshotId(snapshot.getSnapshotId())
                .snapshotHash(snapshot.getSnapshotHash())
                .generalJson(snapshot.getGeneralJson())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }

    private PropertyVersionDTO.AmenitiesSnapshotDTO toAmenitiesDTO(PropertyAmenitiesSnapshot snapshot) {
        if (snapshot == null) return null;
        return PropertyVersionDTO.AmenitiesSnapshotDTO.builder()
                .snapshotId(snapshot.getSnapshotId())
                .snapshotHash(snapshot.getSnapshotHash())
                .amenitiesJson(snapshot.getAmenitiesJson())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }

    private PropertyVersionDTO.PhotosSnapshotDTO toPhotosDTO(PropertyPhotosSnapshot snapshot) {
        if (snapshot == null) return null;
        return PropertyVersionDTO.PhotosSnapshotDTO.builder()
                .snapshotId(snapshot.getSnapshotId())
                .snapshotHash(snapshot.getSnapshotHash())
                .photosJson(snapshot.getPhotosJson())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }

    private PropertyVersionDTO.RulesSnapshotDTO toRulesDTO(PropertyRulesSnapshot snapshot) {
        if (snapshot == null) return null;
        return PropertyVersionDTO.RulesSnapshotDTO.builder()
                .snapshotId(snapshot.getSnapshotId())
                .snapshotHash(snapshot.getSnapshotHash())
                .childrenAllowed(snapshot.getChildrenAllowed())
                .babiesAllowed(snapshot.getBabiesAllowed())
                .petsAllowed(snapshot.getPetsAllowed())
                .smokingAllowed(snapshot.getSmokingAllowed())
                .eventsAllowed(snapshot.getEventsAllowed())
                .customRules(snapshot.getCustomRules())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}