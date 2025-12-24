// src/main/java/com/realestate/listing/service/PriceHistoryService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.PriceHistory;
import com.realestate.listing.entity.Property;
import com.realestate.listing.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PriceHistoryService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    // === CRUD ===
    public List<PriceHistory> getAllPriceHistories() {
        return priceHistoryRepository.findAll();
    }

    public Optional<PriceHistory> getPriceHistoryById(Integer id) {
        return priceHistoryRepository.findById(id);
    }

    public PriceHistory createPriceHistory(PriceHistory priceHistory) {
        return priceHistoryRepository.save(priceHistory);
    }

    public PriceHistory updatePriceHistory(Integer id, PriceHistory updatedPriceHistory) {
        PriceHistory existing = priceHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Historique de prix non trouvé avec l'ID : " + id));

        updatedPriceHistory.setHistoryId(id);
        return priceHistoryRepository.save(updatedPriceHistory);
    }

    public void deletePriceHistory(Integer id) {
        if (!priceHistoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Historique de prix non trouvé avec l'ID : " + id);
        }
        priceHistoryRepository.deleteById(id);
    }

    // === RECHERCHE PAR PROPRIÉTÉ (triée par date décroissante) ===
    public List<PriceHistory> getByPropertyId(Integer propertyId) {
        return priceHistoryRepository.findByProperty_PropertyIdOrderByChangedAtDesc(propertyId);
    }


    @Transactional
    public void recordPriceChange(Property property, Double oldPrice, Double newPrice, String priceType) {
        if (oldPrice == null && newPrice == null) return;

        PriceHistory history = PriceHistory.builder()
                .property(property)
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .priceType(priceType)
                .changedAt(LocalDateTime.now())
                .build();

        priceHistoryRepository.save(history);
    }
}