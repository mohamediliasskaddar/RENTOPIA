package com.rentaldapp.userservice.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserResponseDTO {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private String walletAdresse;
    private String photoUrl;
    private Boolean emailVerified;
    private Boolean telephoneVerified;
    private Boolean isGuest;
    private Boolean isHost;
    private LocalDateTime createdAt;

    // ✅ NOUVEAU : Liste des langues
    private List<UserLanguageDTO> languages = new ArrayList<>();
}