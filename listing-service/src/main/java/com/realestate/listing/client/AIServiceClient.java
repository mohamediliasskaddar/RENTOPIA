package com.realestate.listing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Client Feign pour communiquer avec le AI Service (FastAPI/Python)
 * Permet d'appeler les modèles ML depuis le listing-service
 */
@FeignClient(
        name = "ai-service",
        url = "${ai.service.url:http://localhost:8090}"
)
public interface AIServiceClient {

    /**
     * 💰 Prédire le prix optimal d'une propriété
     * POST http://localhost:8090/price/predict
     */
    @PostMapping("/price/predict")
    Map<String, Object> predictPrice(@RequestBody Map<String, Object> request);

    /**
     * ⚠️ Évaluer le risque d'un locataire
     * POST http://localhost:8090/scoring/predict
     */
    @PostMapping("/scoring/predict")
    Map<String, Object> scoreRisk(@RequestBody Map<String, Object> request);

    /**
     * 🏠 Obtenir des recommandations de propriétés
     * POST http://localhost:8090/recommend/predict
     */
    @PostMapping("/recommend/predict")
    Map<String, Object> getRecommendations(@RequestBody Map<String, Object> request);
}
