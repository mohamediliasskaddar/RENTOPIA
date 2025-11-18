package com.rentaldapp.userservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "wallet_adresse", nullable = false, unique = true, length = 255)
    private String walletAdresse;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(length = 20)
    private String tel;

    @Column(name = "EmailVerified", columnDefinition = "BOOLEAN DEFAULT 0")
    private Boolean emailVerified = false;

    @Column(name = "TelephoneVerified", columnDefinition = "BOOLEAN DEFAULT 0")
    private Boolean telephoneVerified = false;

    @Column(name = "is_guest", columnDefinition = "BOOLEAN DEFAULT 1")
    private Boolean isGuest = true;

    @Column(name = "is_host", columnDefinition = "BOOLEAN DEFAULT 0")
    private Boolean isHost = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //  Relation OneToMany vers UserLanguage
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserLanguage> userLanguages = new ArrayList<>();
}