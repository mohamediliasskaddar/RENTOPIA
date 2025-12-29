package com.rental.payment.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class FeignConfig {

    /**
     * Niveau de logging Feign
     * NONE : Pas de logs
     * BASIC : Uniquement URL, méthode, temps de réponse
     * HEADERS : BASIC + headers request/response
     * FULL : Tout (body inclus) - Attention en production !
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // En production, utiliser BASIC
    }

    /**
     * Configuration des timeouts
     * connectTimeout : temps max pour établir la connexion
     * readTimeout : temps max pour lire la réponse
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                10, TimeUnit.SECONDS,   // connectTimeout (10s)
                30, TimeUnit.SECONDS,   // readTimeout (30s)
                true                    // followRedirects
        );
    }

    /**
     * Configuration du retry mechanism
     * 3 tentatives max avec délai exponentiel
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                100L,                              // période initiale (100ms)
                TimeUnit.SECONDS.toMillis(3L),     // période max (3s)
                3                                  // max tentatives
        );
    }

    /**
     * Gestion personnalisée des erreurs HTTP
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String message = String.format(
                    "Feign Error - Method: %s, Status: %d, Reason: %s",
                    methodKey, response.status(), response.reason()
            );

            log.error("❌ Feign Client Error: {}", message);

            // Retourner des exceptions personnalisées selon le code HTTP
            return switch (response.status()) {
                case 400 -> new IllegalArgumentException("Bad Request: " + message);
                case 401 -> new SecurityException("Unauthorized: " + message);
                case 403 -> new SecurityException("Forbidden: " + message);
                case 404 -> new IllegalStateException("Service Not Found: " + message);
                case 500 -> new RuntimeException("Internal Server Error: " + message);
                case 503 -> new RuntimeException("Service Unavailable: " + message);
                default -> new RuntimeException(message);
            };
        };
    }

    /**
     * Encoder/Decoder JSON avec Jackson
     */
    @Bean
    public feign.codec.Encoder feignEncoder() {
        return new feign.jackson.JacksonEncoder();
    }

    @Bean
    public feign.codec.Decoder feignDecoder() {
        return new feign.jackson.JacksonDecoder();
    }

    /**
     * Intercepteur pour ajouter des headers communs
     */
    @Bean
    public feign.RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("X-Service-Name", "payment-service");
            requestTemplate.header("X-Service-Version", "1.0");

            // Pour le tracing distribué (optionnel)
            String correlationId = java.util.UUID.randomUUID().toString();
            requestTemplate.header("X-Correlation-ID", correlationId);

            log.debug("📤 Feign Request - Correlation ID: {}", correlationId);
        };
    }

    /**
     * Activation de la compression GZIP
     */
    @Bean
    public feign.RequestInterceptor gzipRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Accept-Encoding", "gzip, deflate");
        };
    }
}