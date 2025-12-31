"""
SCHEMAS PYDANTIC POUR RECOMMENDATIONS
======================================
Validation des données entrantes/sortantes pour l'API
"""

from pydantic import BaseModel, Field
from typing import List, Optional

# =========================
# REQUEST SCHEMAS
# =========================

class RecommendationRequest(BaseModel):
    """
    Requête pour obtenir des recommandations

    Peut être basée sur:
    - Un tenant_id (recommandations personnalisées)
    - Un property_id (propriétés similaires)
    """
    tenant_id: Optional[int] = Field(
        None,
        description="ID du locataire (pour recommandations personnalisées)",
        example=42
    )

    property_id: Optional[int] = Field(
        None,
        description="ID d'une propriété (pour trouver des similaires)",
        example=15
    )

    top_n: int = Field(
        5,
        ge=1,
        le=20,
        description="Nombre de recommandations à retourner (1-20)",
        example=5
    )

    class Config:
        json_schema_extra = {
            "example": {
                "tenant_id": 42,
                "property_id": None,
                "top_n": 5
            }
        }


# =========================
# RESPONSE SCHEMAS
# =========================

class PropertyRecommendation(BaseModel):
    """
    Une propriété recommandée avec ses détails
    """
    property_id: int = Field(
        ...,
        description="ID de la propriété"
    )

    score: float = Field(
        ...,
        description="Score de recommandation (0-1 pour similarité, autre pour popularité)"
    )

    # Détails de la propriété
    surface: Optional[float] = Field(
        None,
        description="Surface en m²"
    )

    rooms: Optional[int] = Field(
        None,
        description="Nombre de chambres"
    )

    amenities_count: Optional[int] = Field(
        None,
        description="Nombre d'équipements"
    )

    avg_rating: Optional[float] = Field(
        None,
        description="Note moyenne (1-5)"
    )

    occupancy_rate: Optional[float] = Field(
        None,
        description="Taux d'occupation (0-1)"
    )

    price_per_night_eur: Optional[int] = Field(
        None,
        description="Prix par nuit en EUR"
    )

    price_per_night_eth: Optional[float] = Field(
        None,
        description="Prix par nuit en ETH"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "property_id": 15,
                "score": 0.87,
                "surface": 85,
                "rooms": 3,
                "amenities_count": 8,
                "avg_rating": 4.4,
                "occupancy_rate": 0.72,
                "price_per_night_eur": 420,
                "price_per_night_eth": 0.12
            }
        }


class RecommendationResponse(BaseModel):
    """
    Réponse contenant les recommandations
    """
    recommendations: List[PropertyRecommendation] = Field(
        ...,
        description="Liste des propriétés recommandées"
    )

    count: int = Field(
        ...,
        description="Nombre de recommandations retournées"
    )

    recommendation_type: str = Field(
        ...,
        description="Type de recommandation (user-based, item-based, popular)",
        example="user-based"
    )

    message: str = Field(
        ...,
        description="Message explicatif",
        example="Recommandations basées sur vos préférences"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "recommendations": [
                    {
                        "property_id": 15,
                        "score": 0.87,
                        "surface": 85,
                        "rooms": 3,
                        "amenities_count": 8,
                        "avg_rating": 4.4,
                        "occupancy_rate": 0.72,
                        "price_per_night_eur": 420,
                        "price_per_night_eth": 0.12
                    }
                ],
                "count": 5,
                "recommendation_type": "user-based",
                "message": "Recommandations basées sur vos préférences"
            }
        }


# =========================
# HELPER FUNCTION
# =========================

def create_recommendation_response(
        recommendations: list,
        recommendation_type: str
) -> RecommendationResponse:
    """
    Crée une réponse de recommandation complète

    Args:
        recommendations: Liste de PropertyRecommendation
        recommendation_type: Type de recommandation

    Returns:
        RecommendationResponse
    """
    # Message selon le type
    messages = {
        "user-based": "Recommandations basées sur vos préférences",
        "item-based": "Propriétés similaires à celle sélectionnée",
        "popular": "Propriétés les plus populaires"
    }

    return RecommendationResponse(
        recommendations=recommendations,
        count=len(recommendations),
        recommendation_type=recommendation_type,
        message=messages.get(recommendation_type, "Recommandations disponibles")
    )