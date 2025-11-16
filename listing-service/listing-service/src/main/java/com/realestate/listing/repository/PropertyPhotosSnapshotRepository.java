// src/main/java/com/realestate/listing/repository/PropertyPhotosSnapshotRepository.java
package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyPhotosSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PropertyPhotosSnapshotRepository extends JpaRepository<PropertyPhotosSnapshot, Integer> {

    Optional<PropertyPhotosSnapshot> findBySnapshotHash(String snapshotHash);
}
