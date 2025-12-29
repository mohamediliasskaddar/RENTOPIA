package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyVersionRepository extends JpaRepository<PropertyVersion, Integer> {
    // retourne tous les versions d'une property
    List<PropertyVersion> findByPropertyIdOrderByNumVersionDesc(Integer propertyId);
    // retourner une version d'une property avec son number
    Optional<PropertyVersion> findByPropertyIdAndNumVersion(Integer propertyId, Integer numVersion);
    // retourne la version actuelle
    Optional<PropertyVersion> findTopByPropertyIdOrderByNumVersionDesc(Integer propertyId);
}