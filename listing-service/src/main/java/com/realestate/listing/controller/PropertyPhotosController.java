// src/main/java/com/realestate/listing/controller/PropertyPhotosController.java
package com.realestate.listing.controller;

import com.realestate.listing.entity.PropertyPhoto;
import com.realestate.listing.service.PropertyPhotosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/photos")
@CrossOrigin(origins = "*")
public class PropertyPhotosController {

    @Autowired
    private PropertyPhotosService photosService;

    @GetMapping
    public List<PropertyPhoto> getAll(@PathVariable Integer propertyId) {
        return photosService.getByPropertyId(propertyId);
    }

    @PostMapping
    public PropertyPhoto create(@PathVariable Integer propertyId, @RequestBody PropertyPhoto photo) {
        return photosService.createPhoto(propertyId, photo);
    }

    @PutMapping("/{photoId}")
    public PropertyPhoto update(@PathVariable Integer photoId, @RequestBody PropertyPhoto photo) {
        return photosService.updatePhoto(photoId, photo);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> delete(@PathVariable Integer photoId) {
        photosService.deletePhoto(photoId);
        return ResponseEntity.ok().build();
    }
}