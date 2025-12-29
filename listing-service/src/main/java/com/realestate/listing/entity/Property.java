package com.realestate.listing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "property")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "user_id")
    private Integer userId;

    private String title;
    private String description;

    @Column(name = "property_type")
    private String propertyType;

    @Column(name = "place_type")
    private String placeType;

    @Column(name = "adresse_line")
    private String adresseLine;

    private String city;
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    private Double latitude;
    private Double longitude;

    @Column(name = "neighborhood_description")
    private String neighborhoodDescription;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "surface_area")
    private Double surfaceArea;

    @Column(name = "max_guests")
    private Integer maxGuests;

    private Integer bedrooms;
    private Integer beds;
    private Integer bathrooms;

    @Column(name = "weekend_price_per_night")
    private Double weekendPricePerNight;

    @Column(name = "Price_per_night")
    private Double PricePerNight;

    @Column(name = "cleaning_fee")
    private Double cleaningFee;

    @Column(name = "pet_fee")
    private Double petFee;

    @Column(name = "platform_fee_percentage")
    private Double platformFeePercentage;

    @Column(name = "min_stay_nights")
    private Integer minStayNights;

    @Column(name = "max_stay_nights")
    private Integer maxStayNights;

    @Column(name = "booking_advance_days")
    private Integer bookingAdvanceDays;

    @Column(name = "check_in_time_start")
    private String checkInTimeStart; // si tu veux validation, transformer en LocalTime et gérer conversion dans DTO

    @Column(name = "check_in_time_end")
    private String checkInTimeEnd;

    @Column(name = "check_out_time")
    private String checkOutTime;

    @Column(name = "instant_booking")
    private Boolean instantBooking;

    @Column(name = "cancellation_policy")
    private String cancellationPolicy;

    public enum PropertyStatus {
        DRAFT, ACTIVE, ARCHIVED, DELETED
    }

    @Enumerated(EnumType.STRING)
    private PropertyStatus status = PropertyStatus.DRAFT;

    @Column(name = "blockchain_property_id")
    private String blockchainPropertyId;

    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations corrigées pour éviter les boucles JSON
    @ManyToMany
    @JoinTable(
            name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PropertyPhoto> photos = new HashSet<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PropertyAvailability> availabilities = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "property_discounts",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "discount_id")
    )
    private Set<Discount> discounts = new HashSet<>();

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private PropertyRule rules;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PriceHistory> priceHistory = new HashSet<>();

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private HostInteractionPreference hostPreferences;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
