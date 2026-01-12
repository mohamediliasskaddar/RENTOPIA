// src/main/java/com/realestate/listing/controller/PropertyRuleController.java
package com.realestate.listing.controller;

import com.realestate.listing.entity.PropertyRule;
import com.realestate.listing.service.PropertyRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/property-rules")

public class PropertyRuleController {

    @Autowired
    private PropertyRuleService ruleService;

    // === CRUD ===
    @GetMapping
    public List<PropertyRule> getAll() {
        return ruleService.getAllRules();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyRule> getById(@PathVariable Integer id) {
        return ruleService.getRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PropertyRule> create(@RequestBody PropertyRule rule) {
        try {
            return ResponseEntity.ok(ruleService.createRule(rule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyRule> update(
            @PathVariable Integer id,
            @RequestBody PropertyRule rule) {
        return ResponseEntity.ok(ruleService.updateRule(id, rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok().build();
    }

    // === RECHERCHE PAR PROPRIÉTÉ ===
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<PropertyRule> getByPropertyId(@PathVariable Integer propertyId) {
        return ruleService.getByPropertyId(propertyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}