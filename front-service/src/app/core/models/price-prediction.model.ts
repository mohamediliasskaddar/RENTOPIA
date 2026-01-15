// src/app/core/models/price-prediction.model.ts

/**
 * DTO pour la requête de prédiction de prix (nouvelle propriété)
 */
export interface PricePredictionRequest {
  surfaceArea: number;
  bedrooms: number;
  amenitiesCount: number;
}

/**
 * Réponse de l'API de prédiction de prix
 */
export interface PricePredictionResponse {
  predicted_price_eth: number;
  predicted_price_eur: number;
  confidence_range_eth?: {
    min: number;
    max: number;
  };
  confidence_range_eur?: {
    min: number;
    max: number;
  };
  eth_eur_rate?: number;
  recommendation?: string;

  // Pour propriété existante uniquement
  property_id?: number;
  current_price_eth?: number;
  current_price_eur?: number;
}
