// src/main/java/com/realestate/listing/controller/HostInteractionPreferenceController.java
package com.realestate.listing.controller;

import com.realestate.listing.entity.HostInteractionPreference;
import com.realestate.listing.service.HostInteractionPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/host-preferences")
@CrossOrigin(origins = "*")
public class HostInteractionPreferenceController {

    @Autowired
    private HostInteractionPreferenceService preferenceService;

    // === CRUD ===
    @GetMapping
    public List<HostInteractionPreference> getAll() {
        return preferenceService.getAllPreferences();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HostInteractionPreference> getById(@PathVariable Integer id) {
        return preferenceService.getPreferenceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HostInteractionPreference> create(@RequestBody HostInteractionPreference preference) {
        try {
            HostInteractionPreference saved = preferenceService.createPreference(preference);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HostInteractionPreference> update(
            @PathVariable Integer id,
            @RequestBody HostInteractionPreference preference) {
        return ResponseEntity.ok(preferenceService.updatePreference(id, preference));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        preferenceService.deletePreference(id);
        return ResponseEntity.ok().build();
    }

    // === RECHERCHE PAR PROPRIÉTÉ ===
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<HostInteractionPreference> getByPropertyId(@PathVariable Integer propertyId) {
        return preferenceService.getByPropertyId(propertyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}