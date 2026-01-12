package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyGeneralSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// PropertyGeneralSnapshotRepository.java
public interface PropertyGeneralSnapshotRepository extends JpaRepository<PropertyGeneralSnapshot, Integer> {
    Optional<PropertyGeneralSnapshot> findBySnapshotHash(String hash);
}