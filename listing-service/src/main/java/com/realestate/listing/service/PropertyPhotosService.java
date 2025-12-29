// src/main/java/com/realestate/listing/service/PropertyPhotosService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.Property;
import com.realestate.listing.entity.PropertyPhoto;
import com.realestate.listing.repository.PropertyPhotosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PropertyPhotosService {

    @Autowired
    private PropertyPhotosRepository photosRepository;

    @Autowired
    private PropertyService propertyService;

    // === CRUD ===
    public List<PropertyPhoto> getByPropertyId(Integer propertyId) {
        return photosRepository.findByProperty_PropertyId(propertyId);
    }

    public PropertyPhoto createPhoto(Integer propertyId, PropertyPhoto photo) {
        Property property = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Propriété non trouvée"));
        photo.setProperty(property);
        return photosRepository.save(photo);
    }

    public PropertyPhoto updatePhoto(Integer photoId, PropertyPhoto updatedPhoto) {
        PropertyPhoto existing = photosRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo non trouvée"));
        existing.setPhotoUrl(updatedPhoto.getPhotoUrl());
        existing.setIsCover(updatedPhoto.getIsCover());
        existing.setDisplayOrder(updatedPhoto.getDisplayOrder());
        return photosRepository.save(existing);
    }

    public void deletePhoto(Integer photoId) {
        photosRepository.deleteById(photoId);
    }

    public void deleteAllByPropertyId(Integer propertyId) {
        photosRepository.deleteByProperty_PropertyId(propertyId);
    }
}