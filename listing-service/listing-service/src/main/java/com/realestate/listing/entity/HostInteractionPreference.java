package com.realestate.listing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "host_interaction_preferences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HostInteractionPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Integer preferenceId;

    @OneToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "interaction_level")
    private String interactionLevel;

    @Column(name = "check_in_method")
    private String checkInMethod;

    @Column(name = "check_in_instructions")
    private String checkInInstructions;
}