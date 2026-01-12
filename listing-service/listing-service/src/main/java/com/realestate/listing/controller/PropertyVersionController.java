// src/main/java/com/realestate/listing/controller/PropertyVersionController.java
package com.realestate.listing.controller;

import com.realestate.listing.entity.PropertyVersion;
import com.realestate.listing.service.PropertyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/property-versions")
@CrossOrigin(origins = "*")
public class PropertyVersionController {

    @Autowired
    private PropertyVersionService versionService;

    // === CRUD ===
    @GetMapping
    public List<PropertyVersion> getAll() {
        return versionService.getAllVersions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyVersion> getById(@PathVariable Integer id) {
        return versionService.getVersionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PropertyVersion> create(@RequestBody PropertyVersion version) {
        try {
            return ResponseEntity.ok(versionService.createVersion(version));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyVersion> update(
            @PathVariable Integer id,
            @RequestBody PropertyVersion version) {
        return ResponseEntity.ok(versionService.updateVersion(id, version));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        versionService.deleteVersion(id);
        return ResponseEntity.ok().build();
    }

    // === RECHERCHES PAR PROPRIÉTÉ ===

    /** Toutes les versions d'une propriété */
    @GetMapping("/property/{propertyId}")
    public List<PropertyVersion> getAllByProperty(@PathVariable Integer propertyId) {
        return versionService.getAllByPropertyId(propertyId);
    }

    /** Version spécifique */
    @GetMapping("/property/{propertyId}/version/{numVersion}")
    public ResponseEntity<PropertyVersion> getByVersion(
            @PathVariable Integer propertyId,
            @PathVariable Integer numVersion) {
        return versionService.getByPropertyIdAndVersion(propertyId, numVersion)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Version actuelle */
    @GetMapping("/property/{propertyId}/current")
    public ResponseEntity<PropertyVersion> getCurrent(@PathVariable Integer propertyId) {
        return versionService.getCurrentVersion(propertyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}