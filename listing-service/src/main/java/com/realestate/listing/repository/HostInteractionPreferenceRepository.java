package com.realestate.listing.repository;

import com.realestate.listing.entity.HostInteractionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostInteractionPreferenceRepository extends JpaRepository<HostInteractionPreference, Integer> {
    Optional<HostInteractionPreference> findByProperty_PropertyId(Integer propertyId);
}