// src/main/java/com/realestate/listing/controller/DiscountController.java
package com.realestate.listing.controller;

import com.realestate.listing.entity.Discount;
import com.realestate.listing.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")

public class DiscountController {

    @Autowired
    private DiscountService discountService;

    // === CRUD ===
    @GetMapping
    public List<Discount> getAllDiscounts() {
        return discountService.getAllDiscounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Integer id) {
        return discountService.getDiscountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Discount createDiscount(@RequestBody Discount discount) {
        return discountService.createDiscount(discount);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable Integer id, @RequestBody Discount discount) {
        return ResponseEntity.ok(discountService.updateDiscount(id, discount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Integer id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok().build();
    }

    // === RECHERCHES ===
    @GetMapping("/property/{propertyId}")
    public List<Discount> getByPropertyId(@PathVariable Integer propertyId) {
        return discountService.getByPropertyId(propertyId);
    }

    @GetMapping("/discountId/{discountId}")
    public List<Discount> getByDiscountId(@PathVariable Integer discountId) {
        return discountService.getByDiscountId(discountId);
    }
}