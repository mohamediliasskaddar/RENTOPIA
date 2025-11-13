package com.rentaldapp.userservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private UserResponseDTO user;

    public JwtResponseDTO(String token, UserResponseDTO user) {
        this.token = token;
        this.user = user;
    }
}