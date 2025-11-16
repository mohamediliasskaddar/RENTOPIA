package com.realestate.listing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer ruleId;

    @OneToOne
    @JoinColumn(name = "property_id")
    private Property property;

    private Boolean childrenAllowed;
    private Boolean babiesAllowed;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private Boolean eventsAllowed;
    private String customRules;
}