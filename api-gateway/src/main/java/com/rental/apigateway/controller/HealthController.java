package com.rental.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/gateway")
public class HealthController {

    // Injection optionnelle au cas où DiscoveryClient n'est pas disponible
    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "API Gateway");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getRegisteredServices() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        // Vérifier si DiscoveryClient est disponible
        if (discoveryClient == null) {
            response.put("totalServices", 0);
            response.put("services", Collections.emptyMap());
            response.put("message", "Service discovery is not enabled");
            return ResponseEntity.ok(response);
        }

        try {
            List<String> services = discoveryClient.getServices();

            Map<String, Object> serviceDetails = services.stream()
                    .collect(Collectors.toMap(
                            serviceName -> serviceName,
                            serviceName -> discoveryClient.getInstances(serviceName)
                                    .stream()
                                    .map(instance -> {
                                        Map<String, Object> instanceInfo = new HashMap<>();
                                        instanceInfo.put("instanceId", instance.getInstanceId());
                                        instanceInfo.put("host", instance.getHost());
                                        instanceInfo.put("port", instance.getPort());
                                        instanceInfo.put("uri", instance.getUri().toString());
                                        instanceInfo.put("metadata", instance.getMetadata());
                                        return instanceInfo;
                                    })
                                    .collect(Collectors.toList())
                    ));

            response.put("totalServices", services.size());
            response.put("services", serviceDetails);

        } catch (Exception e) {
            log.warn("Error retrieving services from discovery client: {}", e.getMessage());
            response.put("totalServices", 0);
            response.put("services", Collections.emptyMap());
            response.put("error", "Unable to retrieve services information");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Rental Platform API Gateway");
        response.put("description", "Gateway for routing requests to microservices");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());

        Map<String, String> features = new HashMap<>();
        features.put("authentication", "JWT based authentication");
        features.put("rateLimiting", "Redis based rate limiting");
        features.put("circuitBreaker", "Resilience4J circuit breaker");
        features.put("loadBalancing", "Client-side load balancing");
        features.put("serviceDiscovery", "Eureka service discovery");

        response.put("features", features);

        return ResponseEntity.ok(response);
    }
}