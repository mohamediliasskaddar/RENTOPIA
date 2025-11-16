package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyRulesSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRulesSnapshotRepository extends JpaRepository<PropertyRulesSnapshot, Integer> {
    Optional<PropertyRulesSnapshot> findBySnapshotHash(String snapshotHash);
}