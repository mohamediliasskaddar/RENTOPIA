package com.rental.apigateway.filter;

import com.rental.apigateway.config.JwtConfig;
import com.rental.apigateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtConfig jwtConfig;

    // Routes publiques qui ne nécessitent pas d'authentification
    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/users/register",
            "/api/users/login",
            "/api/users/verify-email",
            "/api/users/resend-verification",
            "/api/users/forgot-password",
            "/api/users/reset-password",
            "/api/listings/search",
            "/api/listings/public",
            "/actuator/health"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.debug("Processing request for path: {}", path);

            // Vérifier si la route est publique
            if (isPublicRoute(path)) {
                log.debug("Public route, skipping authentication: {}", path);
                return chain.filter(exchange);
            }

            // Vérifier la présence du header Authorization
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for path: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Extraire le token
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
                log.warn("Invalid Authorization header format for path: {}", path);
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(jwtConfig.getPrefix().length());

            // Valider le token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid or expired token for path: {}", path);
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extraire les informations du token
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            log.debug("Token validated for user: {} (ID: {}, Role: {})", username, userId, role);

            // Ajouter les informations de l'utilisateur dans les headers pour les microservices
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Email", username)
                    .header("X-User-Role", role)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

            return chain.filter(modifiedExchange);
        };
    }

    // Vérifier si la route est publique
    private boolean isPublicRoute(String path) {
        return PUBLIC_ROUTES.stream()
                .anyMatch(route -> path.startsWith(route) || path.matches(route.replace("/**", "/.*")));
    }

    // Méthode pour gérer les erreurs
    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Authentication error: {} - Status: {}", error, httpStatus);
        return response.setComplete();
    }

    public static class Config {
        // Configuration si nécessaire
    }
}