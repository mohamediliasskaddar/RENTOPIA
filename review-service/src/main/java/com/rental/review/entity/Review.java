package com.rental.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews") // S'assurer que le nom correspond exactement à la table dans la base
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Integer reservationId;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "review_text", columnDefinition = "TEXT", nullable = false)
    private String reviewText;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations avec Rating (One-to-One)
    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Rating rating;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}