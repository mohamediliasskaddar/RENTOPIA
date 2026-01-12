// src/main/java/com/realestate/listing/repository/PropertyPhotosRepository.java
package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyPhotosRepository extends JpaRepository<PropertyPhoto, Integer> {
    List<PropertyPhoto> findByProperty_PropertyId(Integer propertyId);
    void deleteByProperty_PropertyId(Integer propertyId);
}