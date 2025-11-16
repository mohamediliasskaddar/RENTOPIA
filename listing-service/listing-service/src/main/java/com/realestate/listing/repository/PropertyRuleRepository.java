package com.realestate.listing.repository;

import com.realestate.listing.entity.PropertyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyRuleRepository extends JpaRepository<PropertyRule, Integer> {
    Optional<PropertyRule> findByProperty_PropertyId(Integer propertyId);
}