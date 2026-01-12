// src/main/java/com/realestate/listing/service/AmenityService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.Amenity;
import com.realestate.listing.entity.Property;
import com.realestate.listing.repository.AmenityRepository;
import com.realestate.listing.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    // ======================
    // === CRUD DE BASE ===
    // ======================

    /** Récupère toutes les amenities */
    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
    }

    /** Récupère une amenitie par ID */
    public Optional<Amenity> getAmenityById(Integer id) {
        return amenityRepository.findById(id);
    }

    /** Crée une nouvelle amenitie  */
    public Amenity createAmenute(Amenity amenity) {
        return amenityRepository.save(amenity);
    }

    /** Met à jour une amenitie existante (remplace tout) */
    public Amenity updateAmenity(Integer id, Amenity updatedAmenity) {
        Amenity existing = amenityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commodité non trouvée avec l'ID : " + id));

        updatedAmenity.setAmenityId(id);
        return amenityRepository.save(updatedAmenity);
    }


    /** Supprime une commodité */
    public void deleteAmenity(Integer id) {
        if (!amenityRepository.existsById(id)) {
            throw new IllegalArgumentException("Commodité non trouvée avec l'ID : " +  id);
        }
        amenityRepository.deleteById(id);
    }

    // ============================
    // === RECHERCHES SPÉCIFIQUES ===
    // ============================

    /** Recherche par catégorie (insensible à la casse) */
    public List<Amenity> getByCategory(String category) {
        return amenityRepository.findByCategoryIgnoreCase(category);
    }

    /** Recherche par nom partiel (insensible à la casse) */
    public List<Amenity> searchByName(String name) {
        return amenityRepository.findByNameContainingIgnoreCase(name);
    }


}