package com.rentaldapp.bookingservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration globale pour les Feign Clients
 */
@Configuration
public class FeignConfig {

    /**
     * Niveau de logging pour Feign
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // NONE, BASIC, HEADERS, FULL
    }

    /**
     * Intercepteur pour ajouter le JWT aux requêtes Feign
     * Permet de propager l'authentification entre services
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Récupérer le token JWT du contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getCredentials() != null) {
                String token = authentication.getCredentials().toString();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}