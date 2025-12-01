package com.rental.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Integer reservationId;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @OneToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "reservation_id",
            insertable = false, updatable = false)
    private Review review;

    @Column(name = "rating_value", nullable = false)
    private Double ratingValue;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}