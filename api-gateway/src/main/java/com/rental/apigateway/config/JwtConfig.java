package com.rental.apigateway.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret = "votreSecretKeyTresTresLongueAuMoins256BitsP ourEtreSécurisée123456789";
    private long expiration = 86400000; // 24 heures en millisecondes
    private String header = "Authorization";
    private String prefix = "Bearer ";
}