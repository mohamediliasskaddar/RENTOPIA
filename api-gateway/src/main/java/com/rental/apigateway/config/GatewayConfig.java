package com.rental.apigateway.config;

import com.rental.apigateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service - Routes protégées
                .route("user-service-protected", r -> r
                        .path("/api/users/profile/**", "/api/users/update/**", "/api/users/delete/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://user-service"))

                // User Service - Routes publiques
                .route("user-service-public", r -> r
                        .path("/api/users/register", "/api/users/login", "/api/users/verify-email/**")
                        .uri("lb://user-service"))

                // Listing Service - Routes protégées
                .route("listing-service-protected", r -> r
                        .path("/api/listings/create", "/api/listings/update/**", "/api/listings/delete/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://listing-service"))

                // Listing Service - Routes publiques
                .route("listing-service-public", r -> r
                        .path("/api/listings/search", "/api/listings/public/**", "/api/listings/{id}")
                        .uri("lb://listing-service"))

                // Booking Service - Toutes les routes sont protégées
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://booking-service"))

                // Payment Service - Toutes les routes sont protégées
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://payment-service"))

                // Messaging Service - Toutes les routes sont protégées
                .route("messaging-service", r -> r
                        .path("/api/messages/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://messaging-service"))

                // Notification Service - Toutes les routes sont protégées
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://notification-service"))

                // Review Service - Routes mixtes
                .route("review-service-protected", r -> r
                        .path("/api/reviews/create", "/api/reviews/update/**", "/api/reviews/delete/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://review-service"))

                .route("review-service-public", r -> r
                        .path("/api/reviews/property/**", "/api/reviews/public/**")
                        .uri("lb://review-service"))

                // Media Service - Toutes les routes sont protégées
                .route("media-service", r -> r
                        .path("/api/media/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://media-service"))

                // Blockchain Service - Toutes les routes sont protégées
                .route("blockchain-service", r -> r
                        .path("/api/blockchain/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://blockchain-service"))

                .build();
    }
}