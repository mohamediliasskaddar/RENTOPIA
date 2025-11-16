package com.realestate.listing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "discounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Integer discountId;


    @ManyToMany(mappedBy = "discounts")
    @JsonIgnore
    private Set<Property> properties = new HashSet<>();

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Column(name = "min_nights")
    private Integer minNights;

    @Column(name = "description")
    private String description;


}