// src/main/java/com/realestate/listing/controller/PropertyVersionController.java
package com.realestate.listing.controller;

import com.realestate.listing.dto.PropertyVersionDTO;
import com.realestate.listing.entity.PropertyVersion;
import com.realestate.listing.mapper.PropertyVersionMapper;
import com.realestate.listing.service.PropertyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/property-versions")
public class PropertyVersionController {

    @Autowired
    private PropertyVersionService versionService;

    @Autowired
    private PropertyVersionMapper versionMapper;

    // === CRUD avec DTO ===

    @GetMapping
    public List<PropertyVersionDTO> getAll() {
        return versionService.getAllVersions().stream()
                .map(versionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyVersionDTO> getById(@PathVariable Integer id) {
        return versionService.getVersionByIdWithSnapshots(id)  // ← Changé
                .map(versionMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ... autres méthodes CREATE, UPDATE, DELETE restent les mêmes ...

    /** Toutes les versions d'une propriété */
    @GetMapping("/property/{propertyId}")
    public List<PropertyVersionDTO> getAllByProperty(@PathVariable Integer propertyId) {
        return versionService.getAllByPropertyId(propertyId).stream()
                .map(versionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /** Version spécifique */
    @GetMapping("/property/{propertyId}/version/{numVersion}")
    public ResponseEntity<PropertyVersionDTO> getByVersion(
            @PathVariable Integer propertyId,
            @PathVariable Integer numVersion) {
        return versionService.getByPropertyIdAndVersion(propertyId, numVersion)
                .map(versionMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Version actuelle */
    @GetMapping("/property/{propertyId}/current")
    public ResponseEntity<PropertyVersionDTO> getCurrent(@PathVariable Integer propertyId) {
        return versionService.getCurrentVersion(propertyId)
                .map(versionMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}