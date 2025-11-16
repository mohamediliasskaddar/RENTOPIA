package com.realestate.listing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Integer photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "is_cover")
    private Boolean isCover = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "photo_hash")
    private String photoHash;
}