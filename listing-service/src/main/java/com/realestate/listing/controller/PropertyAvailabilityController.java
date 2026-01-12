// src/main/java/com/realestate/listing/controller/PropertyAvailabilityController.java
package com.realestate.listing.controller;

import com.realestate.listing.entity.PropertyAvailability;
import com.realestate.listing.service.PropertyAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/properties/{propertyId}/availability")
public class PropertyAvailabilityController {

    @Autowired private PropertyAvailabilityService service;

    @PostMapping("/block")
    public ResponseEntity<Void> block(
            @PathVariable Integer propertyId,
            @RequestBody BlockRequest request) {
        service.blockPeriod(propertyId, request.start(), request.end(), request.reason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock")  // ✅ POST au lieu de DELETE
    public ResponseEntity<Void> unblock(
            @PathVariable Integer propertyId,
            @RequestBody BlockRequest request) {
        service.unblockPeriod(propertyId, request.start(), request.end());
        return ResponseEntity.ok().build();
    }


    @GetMapping("/blocked")
    public List<PropertyAvailability> getBlocked(
            @PathVariable Integer propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.getBlockedPeriods(propertyId, start, end);
    }

    @GetMapping("/conflict")
    public ResponseEntity<Boolean> hasConflict(
            @PathVariable Integer propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.hasConflict(propertyId, start, end));
    }
}

record BlockRequest(LocalDate start, LocalDate end, String reason) {}