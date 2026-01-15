package com.realestate.listing.service;

import com.realestate.listing.client.AIServiceClient;
import com.realestate.listing.entity.Property;
import com.realestate.listing.repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour interagir avec le AI Service (Price Prediction)
 * Prépare les données et appelle le modèle ML
 */
@Service
public class PricePredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PricePredictionService.class);

    @Autowired
    private AIServiceClient aiServiceClient;

    @Autowired
    private PropertyRepository propertyRepository;

    /**
     * 💰 Suggérer le prix optimal d'une propriété
     *
     * @param propertyId ID de la propriété
     * @return Résultat de la prédiction avec prix ETH et EUR
     */
    public Map<String, Object> suggestOptimalPrice(Integer propertyId) {
        logger.info("🤖 Requesting price prediction for property {}", propertyId);

        try {
            // 1. Récupérer la propriété
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

            // 2. Préparer les données pour le modèle AI
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("surface", property.getSurfaceArea() != null ? property.getSurfaceArea() : 80.0);
            aiRequest.put("rooms", property.getBedrooms() != null ? property.getBedrooms() : 2);
            aiRequest.put("amenities_count", calculateAmenitiesCount(property));
            aiRequest.put("avg_rating", calculateAvgRating(propertyId)); // TODO: Implémenter avec reviews
            aiRequest.put("occupancy_rate", calculateOccupancyRate(propertyId)); // TODO: Implémenter

            logger.info("📤 Sending to AI Service: {}", aiRequest);

            // 3. Appeler le AI Service
            Map<String, Object> prediction = aiServiceClient.predictPrice(aiRequest);

            logger.info("📥 AI Prediction received: {} ETH ({}€)",
                    prediction.get("predicted_price_eth"),
                    prediction.get("predicted_price_eur"));

            // 4. Enrichir avec les données de la propriété
            Map<String, Object> response = new HashMap<>(prediction);
            response.put("property_id", propertyId);
            response.put("current_price_eth", convertEurToEth(property.getPricePerNight()));
            response.put("current_price_eur", property.getPricePerNight());

            return response;

        } catch (Exception e) {
            logger.error("❌ Failed to predict price for property {}", propertyId, e);
            throw new RuntimeException("Erreur lors de la prédiction du prix: " + e.getMessage(), e);
        }
    }

    /**
     * 💰 Suggérer le prix pour une NOUVELLE propriété (avant création)
     * Utilisé dans le formulaire de création
     */
    public Map<String, Object> suggestPriceForNewProperty(
            Double surfaceArea,
            Integer bedrooms,
            Integer amenitiesCount) {

        logger.info("🤖 Predicting price for NEW property");

        try {
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("surface", surfaceArea != null ? surfaceArea : 80.0);
            aiRequest.put("rooms", bedrooms != null ? bedrooms : 2);
            aiRequest.put("amenities_count", amenitiesCount != null ? amenitiesCount : 5);
            aiRequest.put("avg_rating", 4.0); // Valeur par défaut pour nouvelle propriété
            aiRequest.put("occupancy_rate", 0.7); // Estimation initiale

            logger.info("📤 Sending to AI Service: {}", aiRequest);

            Map<String, Object> prediction = aiServiceClient.predictPrice(aiRequest);

            logger.info("📥 Prediction: {} ETH ({}€)",
                    prediction.get("predicted_price_eth"),
                    prediction.get("predicted_price_eur"));

            return prediction;

        } catch (Exception e) {
            logger.error("❌ Failed to predict price", e);
            throw new RuntimeException("Erreur lors de la prédiction: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // MÉTHODES HELPER POUR CALCULER LES MÉTRIQUES
    // ============================================================

    /**
     * Compter le nombre d'équipements de la propriété
     */
    private int calculateAmenitiesCount(Property property) {
        if (property.getAmenities() == null) {
            return 5; // Valeur par défaut
        }
        return property.getAmenities().size();
    }

    /**
     * Calculer la note moyenne (à partir des reviews)
     * TODO: À implémenter quand tu auras un service de reviews
     */
    private double calculateAvgRating(Integer propertyId) {
        // TODO: Récupérer depuis review-service ou une table reviews
        // Pour l'instant, valeur par défaut
        return 4.0;
    }

    /**
     * Calculer le taux d'occupation
     * (Nombre de jours réservés / Nombre de jours disponibles)
     * TODO: À implémenter avec les données de réservation
     */
    private double calculateOccupancyRate(Integer propertyId) {
        // TODO: Calculer depuis booking-service
        // Formule: (jours réservés / jours disponibles)
        // Pour l'instant, valeur par défaut
        return 0.7;
    }

    /**
     * Convertir EUR en ETH (taux fixe temporaire)
     */
    private double convertEurToEth(Double priceEur) {
        if (priceEur == null) return 0.0;
        // Taux de conversion: 1 ETH = 3500 EUR (ajuster selon le marché)
        return priceEur / 3500.0;
    }
}
