package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyAmenitiesSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyAmenitiesSnapshotRepository extends JpaRepository<PropertyAmenitiesSnapshot, Integer> {

    Optional<PropertyAmenitiesSnapshot> findBySnapshotHash(String snapshotHash);
}