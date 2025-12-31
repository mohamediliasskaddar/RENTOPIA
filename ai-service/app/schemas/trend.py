"""
SCHEMAS PYDANTIC POUR MARKET TREND
===================================
Validation des données entrantes/sortantes pour l'API
"""

from pydantic import BaseModel, Field
from typing import List, Optional

# =========================
# RESPONSE SCHEMAS
# =========================

class NeighborhoodTrend(BaseModel):
    """
    Tendance d'un quartier spécifique
    """
    neighborhood_id: int = Field(
        ...,
        description="ID du quartier"
    )

    current_avg_price_eth: float = Field(
        ...,
        description="Prix moyen actuel (ETH)"
    )

    trend_label: str = Field(
        ...,
        description="Type de tendance (RISING, STABLE, DECLINING)"
    )

    slope: float = Field(
        ...,
        description="Pente de la tendance"
    )

    volatility: float = Field(
        ...,
        description="Volatilité du marché"
    )

    predicted_price_3m_eth: float = Field(
        ...,
        description="Prix prédit à 3 mois (ETH)"
    )

    predicted_price_6m_eth: float = Field(
        ...,
        description="Prix prédit à 6 mois (ETH)"
    )

    confidence: str = Field(
        ...,
        description="Niveau de confiance (HIGH, MEDIUM, LOW)"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "neighborhood_id": 0,
                "current_avg_price_eth": 0.2291,
                "trend_label": "STABLE",
                "slope": 0.000267,
                "volatility": 0.0702,
                "predicted_price_3m_eth": 0.2299,
                "predicted_price_6m_eth": 0.2307,
                "confidence": "MEDIUM"
            }
        }


class MarketTrendResponse(BaseModel):
    """
    Réponse contenant les tendances du marché
    """
    neighborhoods: List[NeighborhoodTrend] = Field(
        ...,
        description="Liste des tendances par quartier"
    )

    count: int = Field(
        ...,
        description="Nombre de quartiers analysés"
    )

    summary: dict = Field(
        ...,
        description="Résumé global du marché"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "neighborhoods": [
                    {
                        "neighborhood_id": 0,
                        "current_avg_price_eth": 0.2291,
                        "trend_label": "STABLE",
                        "slope": 0.000267,
                        "volatility": 0.0702,
                        "predicted_price_3m_eth": 0.2299,
                        "predicted_price_6m_eth": 0.2307,
                        "confidence": "MEDIUM"
                    }
                ],
                "count": 10,
                "summary": {
                    "avg_price": 0.2200,
                    "rising_neighborhoods": 2,
                    "stable_neighborhoods": 6,
                    "declining_neighborhoods": 2
                }
            }
        }


class HeatmapData(BaseModel):
    """
    Données formatées pour une heatmap
    """
    neighborhoods: List[str] = Field(
        ...,
        description="Noms des quartiers"
    )

    current_prices: List[float] = Field(
        ...,
        description="Prix actuels (ETH)"
    )

    predicted_prices_3m: List[float] = Field(
        ...,
        description="Prix prédits à 3 mois (ETH)"
    )

    trend_labels: List[str] = Field(
        ...,
        description="Labels de tendance"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "neighborhoods": ["Quartier 0", "Quartier 1", "Quartier 2"],
                "current_prices": [0.2291, 0.2068, 0.2391],
                "predicted_prices_3m": [0.2299, 0.2077, 0.2402],
                "trend_labels": ["STABLE", "STABLE", "STABLE"]
            }
        }


# =========================
# HELPER FUNCTIONS
# =========================

def create_market_trend_response(trends: list) -> MarketTrendResponse:
    """
    Crée une réponse Market Trend complète

    Args:
        trends: Liste de NeighborhoodTrend

    Returns:
        MarketTrendResponse
    """
    # Calculer le résumé
    rising = sum(1 for t in trends if t.get('trend_label') == 'RISING')
    stable = sum(1 for t in trends if t.get('trend_label') == 'STABLE')
    declining = sum(1 for t in trends if t.get('trend_label') == 'DECLINING')

    avg_price = sum(t.get('current_avg_price_eth', 0) for t in trends) / len(trends)

    summary = {
        "avg_price": round(avg_price, 4),
        "rising_neighborhoods": rising,
        "stable_neighborhoods": stable,
        "declining_neighborhoods": declining,
        "total_neighborhoods": len(trends)
    }

    neighborhoods = [NeighborhoodTrend(**trend) for trend in trends]

    return MarketTrendResponse(
        neighborhoods=neighborhoods,
        count=len(neighborhoods),
        summary=summary
    )