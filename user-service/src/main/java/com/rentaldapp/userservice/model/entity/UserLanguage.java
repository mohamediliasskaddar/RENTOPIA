package com.rentaldapp.userservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_languages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "language_id", nullable = false)
    private Integer languageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", length = 20)
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.INTERMEDIATE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relation ManyToOne vers Language
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "language_id", insertable = false, updatable = false)
    private Language language;

    public enum ProficiencyLevel {
        BASIC,
        INTERMEDIATE,
        ADVANCED,
        NATIVE
    }
}