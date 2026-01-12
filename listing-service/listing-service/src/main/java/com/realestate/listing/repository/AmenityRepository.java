package com.realestate.listing.repository;

import com.realestate.listing.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Integer> {
    List<Amenity> findByCategoryIgnoreCase(String category);
    List<Amenity> findByNameContainingIgnoreCase(String name);
}