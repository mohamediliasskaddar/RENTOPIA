package com.rental.configserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/config")
public class HealthController {

    @Value("${spring.cloud.config.server.git.uri}")
    private String gitUri;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Config Server");
        response.put("version", "1.0.0");
        response.put("gitUri", gitUri);

        // Vérifier si le dépôt Git existe
        String repoPath = gitUri.replace("file://", "");
        File repoDir = new File(repoPath);
        response.put("gitRepoExists", repoDir.exists());

        log.info("Health check requested");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Rental Platform Config Server");
        response.put("description", "Centralized Configuration Management");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        response.put("gitUri", gitUri);

        return ResponseEntity.ok(response);
    }
}