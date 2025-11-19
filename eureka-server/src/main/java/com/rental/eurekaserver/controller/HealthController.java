package com.rental.eurekaserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/eureka")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Eureka Server");
        response.put("version", "1.0.0");
        response.put("port", 8761);

        log.info("Health check requested");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Rental Platform Eureka Server");
        response.put("description", "Service Discovery and Registration Server");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        response.put("dashboardUrl", "http://localhost:8761");

        return ResponseEntity.ok(response);
    }
}