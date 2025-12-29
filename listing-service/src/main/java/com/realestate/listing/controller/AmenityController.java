package com.realestate.listing.controller;

import com.realestate.listing.entity.Amenity;
import com.realestate.listing.entity.Discount;
import com.realestate.listing.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/amenities")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    @GetMapping("/all")
    public List<Amenity> getAll() {
        return amenityService.getAllAmenities();
    }

    @GetMapping("/category/{category}")
    public List<Amenity> getByCategory(@PathVariable String category) {
        return amenityService.getByCategory(category);
    }

    @GetMapping("/search")
    public List<Amenity> search(@RequestParam String name) {
        return amenityService.searchByName(name);
    }
    @PostMapping
    public Amenity createAmenity(@RequestBody Amenity amenity) {
        return amenityService.createAmenute(amenity);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Integer id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.ok().build();
    }
}
