"""
ROUTER PRICE PREDICTION (VERSION ETH)
======================================
Endpoints pour la prédiction de prix en ETH
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List
from app.schemas.price import (
    PricePredictionRequest,
    PricePredictionResponse,
    create_price_response
)
from app.services.price_model import PricePredictionModel

# =========================
# ROUTER SETUP
# =========================
router = APIRouter()

# Charger le modèle au démarrage du router
try:
    model = PricePredictionModel()
except FileNotFoundError as e:
    print(f"⚠️  {e}")
    model = None


# =========================
# ENDPOINTS
# =========================

@router.post(
    "/predict",
    response_model=PricePredictionResponse,
    summary="Prédire le prix d'une propriété EN ETH",
    description="""
    Prédit le prix par nuit d'une propriété en ETH (blockchain-native).
    
    **Entrées:**
    - surface (m²)
    - rooms (nombre de chambres)
    - amenities_count (équipements)
    - avg_rating (note 1-5)
    - occupancy_rate (taux d'occupation 0-1)
    
    **Sorties:**
    - predicted_price_eth (prix en ETH)
    - confidence_range_eth (fourchette min/max)
    - predicted_price_eur (bonus: prix en EUR)
    - recommendation (conseil textuel)
    """
)
def predict_price(request: PricePredictionRequest):
    """
    Endpoint principal pour la prédiction de prix

    Utilisé par:
    - Backend (booking-service)
    - Frontend (React)
    - Smart Contract (via backend)
    """
    # Vérifier que le modèle est chargé
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle Price Prediction non disponible. Lancez: python app/services/price_model.py"
        )

    try:
        # Convertir la requête en dict
        data = request.model_dump()

        # Prédiction avec fourchette de confiance
        result = model.predict_with_confidence(data)

        # Créer la réponse complète (ETH + EUR)
        response = create_price_response(
            price_eth=result["price_eth"],
            confidence_range_eth=result["confidence_range"]
        )

        return response

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la prédiction: {str(e)}"
        )


@router.get(
    "/example",
    response_model=PricePredictionResponse,
    summary="Exemple de prédiction",
    description="Retourne une prédiction pour une propriété exemple (85m², 3 chambres)"
)
def get_example_prediction():
    """
    Endpoint de démonstration

    Utile pour:
    - Tester rapidement l'API
    - Démo en soutenance
    - Frontend (affichage d'exemple)
    """
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle non disponible"
        )

    # Propriété exemple
    example_property = PricePredictionRequest(
        surface=85,
        rooms=3,
        amenities_count=8,
        avg_rating=4.4,
        occupancy_rate=0.72
    )

    # Utiliser l'endpoint principal
    return predict_price(example_property)


@router.get(
    "/health",
    summary="Vérifier le statut du modèle",
    description="Vérifie si le modèle Price Prediction est chargé et opérationnel"
)
def health_check():
    """
    Health check spécifique au modèle Price

    Utilisé par:
    - DevOps (monitoring)
    - Docker health checks
    - Kubernetes probes
    """
    if model is None:
        return {
            "status": "DOWN",
            "model": "price_prediction",
            "loaded": False,
            "message": "Modèle non chargé"
        }

    return {
        "status": "UP",
        "model": "price_prediction",
        "loaded": True,
        "message": "Modèle opérationnel"
    }


# =========================
# ENDPOINT BONUS: BATCH PREDICTION
# =========================

class BatchPricePredictionRequest(BaseModel):
    """Prédire plusieurs propriétés en une requête"""
    properties: List[PricePredictionRequest]


class BatchPricePredictionResponse(BaseModel):
    """Résultats multiples"""
    predictions: List[PricePredictionResponse]
    count: int


@router.post(
    "/predict/batch",
    response_model=BatchPricePredictionResponse,
    summary="Prédiction en lot (multiple propriétés)",
    description="Prédit le prix de plusieurs propriétés en une seule requête"
)
def predict_price_batch(request: BatchPricePredictionRequest):
    """
    Endpoint pour prédictions multiples

    Utile pour:
    - Comparer plusieurs propriétés
    - Import en masse
    - Analyses de marché
    """
    if model is None:
        raise HTTPException(status_code=503, detail="Modèle non disponible")

    try:
        predictions = []

        for prop in request.properties:
            # Prédire chaque propriété
            data = prop.model_dump()
            result = model.predict_with_confidence(data)

            response = create_price_response(
                price_eth=result["price_eth"],
                confidence_range_eth=result["confidence_range"]
            )

            predictions.append(response)

        return BatchPricePredictionResponse(
            predictions=predictions,
            count=len(predictions)
        )

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors des prédictions: {str(e)}"
        )