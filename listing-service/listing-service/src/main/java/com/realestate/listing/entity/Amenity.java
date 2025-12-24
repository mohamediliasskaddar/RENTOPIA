package com.realestate.listing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "amenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id")
    private Integer amenityId;

    private String name;
    private String category;
    private String icone;

    @ManyToMany(mappedBy = "amenities")
    @JsonIgnore
    private Set<Property> properties = new HashSet<>();
}
