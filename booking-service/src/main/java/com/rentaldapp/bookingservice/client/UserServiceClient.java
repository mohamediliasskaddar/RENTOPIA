package com.rentaldapp.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Client Feign pour communiquer avec le User Service
 * ✅ CORRECTION : URL corrigée + méthodes manquantes ajoutées
 */
@FeignClient(name = "user-service", url = "${services.user-service.url:http://localhost:8081}")
public interface UserServiceClient {

    /**
     * Récupérer les informations complètes d'un utilisateur
     */
    @GetMapping("/api/v1/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") Integer id);

    /**
     * ✅ NOUVEAU : Récupérer uniquement l'email (pour notifications)
     */
    @GetMapping("/api/v1/users/{id}/email")
    String getUserEmail(@PathVariable("id") Integer id);

    /**
     * ✅ NOUVEAU : Récupérer l'adresse wallet (pour paiements blockchain)
     */
    @GetMapping("/api/v1/users/{id}/wallet")
    String getUserWallet(@PathVariable("id") Integer id);

    /**
     * ✅ NOUVEAU : Vérifier si un utilisateur existe
     */
    @GetMapping("/api/v1/users/{id}/exists")
    Boolean userExists(@PathVariable("id") Integer id);
}