// src/main/java/com/realestate/listing/controller/PriceHistoryController.java
package com.realestate.listing.controller;

import com.realestate.listing.entity.PriceHistory;
import com.realestate.listing.service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price-history")

public class PriceHistoryController {

    @Autowired
    private PriceHistoryService priceHistoryService;

    // === CRUD ===
    @GetMapping
    public List<PriceHistory> getAll() {
        return priceHistoryService.getAllPriceHistories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceHistory> getById(@PathVariable Integer id) {
        return priceHistoryService.getPriceHistoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public PriceHistory create(@RequestBody PriceHistory priceHistory) {
        return priceHistoryService.createPriceHistory(priceHistory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceHistory> update(
            @PathVariable Integer id,
            @RequestBody PriceHistory priceHistory) {
        return ResponseEntity.ok(priceHistoryService.updatePriceHistory(id, priceHistory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        priceHistoryService.deletePriceHistory(id);
        return ResponseEntity.ok().build();
    }

    // === RECHERCHE PAR PROPRIÉTÉ (triée par date décroissante) ===
    @GetMapping("/property/{propertyId}")
    public List<PriceHistory> getByPropertyId(@PathVariable Integer propertyId) {
        return priceHistoryService.getByPropertyId(propertyId);
    }
}