// src/main/java/com/realestate/listing/service/PropertyRuleService.java
package com.realestate.listing.service;

import com.realestate.listing.entity.PropertyRule;
import com.realestate.listing.repository.PropertyRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyRuleService {

    @Autowired
    private PropertyRuleRepository ruleRepository;

    // === CRUD ===
    public List<PropertyRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public Optional<PropertyRule> getRuleById(Integer id) {
        return ruleRepository.findById(id);
    }

    public PropertyRule createRule(PropertyRule rule) {
        Integer propertyId = rule.getProperty().getPropertyId();
        if (ruleRepository.findByProperty_PropertyId(propertyId).isPresent()) {
            throw new IllegalArgumentException("Une règle existe déjà pour la propriété ID : " + propertyId);
        }
        return ruleRepository.save(rule);
    }

    public PropertyRule updateRule(Integer id, PropertyRule updatedRule) {
        PropertyRule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Règle non trouvée avec l'ID : " + id));

        updatedRule.setRuleId(id);
        return ruleRepository.save(updatedRule);
    }

    public void deleteRule(Integer id) {
        if (!ruleRepository.existsById(id)) {
            throw new IllegalArgumentException("Règle non trouvée avec l'ID : " + id);
        }
        ruleRepository.deleteById(id);
    }

    // === RECHERCHE PAR PROPRIÉTÉ ===
    public Optional<PropertyRule> getByPropertyId(Integer propertyId) {
        return ruleRepository.findByProperty_PropertyId(propertyId);
    }
}