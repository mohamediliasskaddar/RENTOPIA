package com.rentaldapp.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginDTO {
    @NotBlank(message = "L'adresse wallet est obligatoire")
    private String walletAdresse;

    @NotBlank(message = "La signature est obligatoire")
    private String signature;


}