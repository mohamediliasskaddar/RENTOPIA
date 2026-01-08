package com.rentaldapp.bookingservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("=== JWT Filter ===");
        logger.info("Request URI: " + request.getRequestURI());
        logger.info("Method: " + request.getMethod());

        try {
            String jwt = getJwtFromRequest(request);
            logger.info("JWT extracted: " + (jwt != null ? "present" : "null"));

            if (StringUtils.hasText(jwt)) {
                logger.info("JWT token: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
                boolean isValid = jwtTokenProvider.validateToken(jwt);
                logger.info("JWT validation result: " + isValid);

                if (isValid) {
                    String email = jwtTokenProvider.getEmailFromToken(jwt);
                    Integer userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    logger.info("Email from token: " + email);
                    logger.info("User ID from token: " + userId);

                    // Utiliser userId comme principal (converti en String pour compatibilité)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId.toString(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("Authentication set in SecurityContext for user ID: " + userId);
                } else {
                    logger.warn("JWT token is invalid");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                    return;
                }
            } else {
                logger.warn("No JWT token found in request");
                // Ne pas bloquer la requête ici, laisse Spring Security gérer l'authentification requise
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // Vérifiez l'authentification après traitement
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.info("Final Authentication: " + SecurityContextHolder.getContext().getAuthentication().getName());
        } else {
            logger.info("Final Authentication: NULL");
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.info("Authorization header: " + (bearerToken != null ? bearerToken.substring(0, Math.min(20, bearerToken.length())) + "..." : "null"));
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}