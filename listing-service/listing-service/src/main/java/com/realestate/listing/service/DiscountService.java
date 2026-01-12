// src/main/java/com/realestate/listing/service/DiscountService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.Discount;
import com.realestate.listing.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    // === CRUD ===
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public Optional<Discount> getDiscountById(Integer id) {
        return discountRepository.findById(id);
    }

    public Discount createDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

    public Discount updateDiscount(Integer id, Discount updatedDiscount) {
        Discount existing = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réduction non trouvée avec l'ID : " + id));
        updatedDiscount.setDiscountId(id);
        return discountRepository.save(updatedDiscount);
    }

    public void deleteDiscount(Integer id) {
        if (!discountRepository.existsById(id)) {
            throw new IllegalArgumentException("Réduction non trouvée avec l'ID : " + id);
        }
        discountRepository.deleteById(id);
    }

    // === RECHERCHES SPÉCIFIQUES ===
    public List<Discount> getByDiscountId(Integer discountId) {
        return discountRepository.findByDiscountId(discountId);
    }

    public List<Discount> getByPropertyId(Integer propertyId) {
        return discountRepository.findByProperties_PropertyId(propertyId);
    }
}