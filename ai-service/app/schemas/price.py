"""
SCHEMAS PYDANTIC POUR PRICE PREDICTION (ETH)
=============================================
Validation des données entrantes/sortantes pour l'API
"""

from pydantic import BaseModel, Field
from typing import Optional

# Taux de conversion (même que dans le modèle)
ETH_EUR_RATE = 3500.0


class PricePredictionRequest(BaseModel):
    """
    Requête pour prédire le prix d'une propriété

    Envoyée par le backend/frontend
    """
    surface: float = Field(
        ...,
        gt=0,
        le=500,
        description="Surface de la propriété en m²",
        example=85
    )

    rooms: int = Field(
        ...,
        ge=1,
        le=10,
        description="Nombre de chambres",
        example=3
    )

    amenities_count: int = Field(
        ...,
        ge=0,
        le=20,
        description="Nombre d'équipements (Wi-Fi, parking, etc.)",
        example=8
    )

    avg_rating: float = Field(
        ...,
        ge=1.0,
        le=5.0,
        description="Note moyenne de la propriété (1-5)",
        example=4.4
    )

    occupancy_rate: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Taux d'occupation (0.0 = vide, 1.0 = plein)",
        example=0.72
    )

    class Config:
        json_schema_extra = {
            "example": {
                "surface": 85,
                "rooms": 3,
                "amenities_count": 8,
                "avg_rating": 4.4,
                "occupancy_rate": 0.72
            }
        }


class ConfidenceRange(BaseModel):
    """
    Fourchette de prix (min/max)
    """
    min: float = Field(
        ...,
        description="Prix minimum (confiance 90%)"
    )
    max: float = Field(
        ...,
        description="Prix maximum (confiance 90%)"
    )


class PricePredictionResponse(BaseModel):
    """
    Réponse contenant le prix prédit

    🔥 VERSION BLOCKCHAIN-NATIVE: Prix en ETH
    📊 BONUS: Prix en EUR pour lisibilité humaine
    """
    # 🔥 Prix en ETH (PRINCIPAL)
    predicted_price_eth: float = Field(
        ...,
        description="Prix par nuit prédit EN ETH",
        example=0.1205
    )

    confidence_range_eth: ConfidenceRange = Field(
        ...,
        description="Fourchette de prix en ETH (marge ±10%)"
    )

    # 📊 Prix en EUR (BONUS - pour lisibilité)
    predicted_price_eur: Optional[int] = Field(
        None,
        description="Prix par nuit en EUR (pour info)",
        example=422
    )

    confidence_range_eur: Optional[ConfidenceRange] = Field(
        None,
        description="Fourchette de prix en EUR"
    )

    # 💡 Métadonnées
    eth_eur_rate: float = Field(
        ...,
        description="Taux de conversion utilisé (1 ETH = X EUR)",
        example=3500.0
    )

    recommendation: str = Field(
        ...,
        description="Recommandation textuelle basée sur le prix",
        example="Prix standard pour ce type de propriété"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "predicted_price_eth": 0.1205,
                "confidence_range_eth": {
                    "min": 0.1085,
                    "max": 0.1326
                },
                "predicted_price_eur": 422,
                "confidence_range_eur": {
                    "min": 380,
                    "max": 464
                },
                "eth_eur_rate": 3500.0,
                "recommendation": "Prix standard pour ce type de propriété"
            }
        }


# =========================
# HELPER FUNCTIONS
# =========================

def get_price_recommendation(price_eth: float) -> str:
    """
    Génère une recommandation textuelle basée sur le prix

    Args:
        price_eth: prix en ETH

    Returns:
        str: recommandation
    """
    # Conversion en EUR pour compréhension humaine
    price_eur = price_eth * ETH_EUR_RATE

    if price_eur < 200:
        return "Prix économique - Bon rapport qualité/prix"
    elif price_eur < 350:
        return "Prix standard pour ce type de propriété"
    elif price_eur < 500:
        return "Prix premium - Propriété de qualité"
    else:
        return "Prix haut de gamme - Propriété d'exception"


def eth_to_eur(eth: float) -> int:
    """Convertit ETH en EUR (arrondi à l'entier)"""
    return int(eth * ETH_EUR_RATE)


def create_price_response(
        price_eth: float,
        confidence_range_eth: dict
) -> PricePredictionResponse:
    """
    Crée une réponse complète avec prix ETH + EUR

    Args:
        price_eth: prix prédit en ETH
        confidence_range_eth: {"min": float, "max": float} en ETH

    Returns:
        PricePredictionResponse
    """
    return PricePredictionResponse(
        # Prix ETH
        predicted_price_eth=price_eth,
        confidence_range_eth=ConfidenceRange(
            min=confidence_range_eth["min"],
            max=confidence_range_eth["max"]
        ),

        # Prix EUR (bonus)
        predicted_price_eur=eth_to_eur(price_eth),
        confidence_range_eur=ConfidenceRange(
            min=eth_to_eur(confidence_range_eth["min"]),
            max=eth_to_eur(confidence_range_eth["max"])
        ),

        # Métadonnées
        eth_eur_rate=ETH_EUR_RATE,
        recommendation=get_price_recommendation(price_eth)
    )