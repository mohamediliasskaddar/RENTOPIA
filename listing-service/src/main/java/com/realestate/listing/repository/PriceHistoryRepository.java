package com.realestate.listing.repository;

import com.realestate.listing.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    List<PriceHistory> findByProperty_PropertyIdOrderByChangedAtDesc(Integer propertyId);
}