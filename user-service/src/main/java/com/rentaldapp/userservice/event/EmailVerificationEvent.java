package com.rentaldapp.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationEvent implements Serializable {
    private String email;
    private String verificationToken;
    private String templateType;
    private Long timestamp;

    // Constructeur pour email + token (sans templateType ni timestamp)
    public EmailVerificationEvent(String email, String verificationToken) {
        this.email = email;
        this.verificationToken = verificationToken;
        this.templateType = "EMAIL_VERIFICATION";
        this.timestamp = System.currentTimeMillis();
    }

    // Constructeur avec tous les paramètres sauf timestamp
    public EmailVerificationEvent(String email, String verificationToken, String templateType) {
        this.email = email;
        this.verificationToken = verificationToken;
        this.templateType = templateType;
        this.timestamp = System.currentTimeMillis();
    }
}