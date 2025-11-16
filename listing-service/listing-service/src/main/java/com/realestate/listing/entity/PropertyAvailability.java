package com.realestate.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "property_availability")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyAvailability {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Integer availabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false; // Toujours false ici

    @Column(name = "because")
    private String because; // "owner_block" | "booked"
}