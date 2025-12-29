package com.realestate.listing.repository;

import com.realestate.listing.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    
    // Toutes les propriétés ayant une réduction donnée
    List<Discount> findByDiscountId(Integer discountId);

    List<Discount> findByProperties_PropertyId(Integer propertyId);
}