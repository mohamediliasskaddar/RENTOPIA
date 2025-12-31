"""
ROUTER MARKET TREND
===================
Endpoints pour les tendances du marché immobilier
"""

from fastapi import APIRouter, HTTPException, Query, Path
from typing import Optional
from app.schemas.trend import (
    MarketTrendResponse,
    NeighborhoodTrend,
    HeatmapData,
    create_market_trend_response
)
from app.services.trend_model import MarketTrendModel

# =========================
# ROUTER SETUP
# =========================
router = APIRouter()

# Charger le modèle au démarrage du router
try:
    model = MarketTrendModel()
except FileNotFoundError as e:
    print(f"⚠️  {e}")
    model = None


# =========================
# ENDPOINTS
# =========================

@router.get(
    "/trends",
    response_model=MarketTrendResponse,
    summary="Obtenir les tendances de tous les quartiers",
    description="""
    Retourne les tendances de marché pour tous les quartiers.
    
    **Inclut:**
    - Prix actuel moyen par quartier
    - Type de tendance (RISING, STABLE, DECLINING)
    - Prédictions à 3 et 6 mois
    - Niveau de confiance
    - Résumé global du marché
    
    **Algorithme:** Time-series clustering avec KMeans
    """
)
def get_all_trends():
    """
    Endpoint principal pour obtenir toutes les tendances

    Utilisé par:
    - Dashboard frontend
    - Analyses de marché
    - Rapports
    """
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle Market Trend non disponible. Lancez: python app/services/trend_model.py"
        )

    try:
        # Obtenir toutes les tendances
        trends = model.get_all_trends()

        # Créer la réponse
        return create_market_trend_response(trends)

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la récupération des tendances: {str(e)}"
        )


@router.get(
    "/trends/{neighborhood_id}",
    response_model=NeighborhoodTrend,
    summary="Obtenir la tendance d'un quartier spécifique",
    description="Retourne les détails de tendance pour un quartier donné"
)
def get_neighborhood_trend(
        neighborhood_id: int = Path(..., ge=0, description="ID du quartier")
):
    """
    Endpoint pour un quartier spécifique

    Utilisé par:
    - Détails d'un quartier
    - Comparaisons
    """
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle non disponible"
        )

    try:
        # Obtenir la tendance du quartier
        trend = model.get_neighborhood_trend(neighborhood_id)

        if trend is None:
            raise HTTPException(
                status_code=404,
                detail=f"Quartier {neighborhood_id} introuvable"
            )

        return NeighborhoodTrend(**trend)

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la récupération: {str(e)}"
        )


@router.get(
    "/heatmap",
    response_model=HeatmapData,
    summary="Obtenir les données pour une heatmap",
    description="""
    Retourne les données formatées pour créer une heatmap de prix.
    
    **Format optimisé pour:**
    - Plotly
    - Seaborn
    - Matplotlib
    - Chart.js
    """
)
def get_heatmap_data():
    """
    Endpoint pour données de heatmap

    Utilisé par:
    - Visualisations frontend
    - Dashboard analytics
    - Rapports visuels
    """
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle non disponible"
        )

    try:
        # Obtenir les données de heatmap
        heatmap = model.get_heatmap_data()

        return HeatmapData(**heatmap)

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la génération de la heatmap: {str(e)}"
        )


@router.get(
    "/summary",
    summary="Résumé global du marché",
    description="Retourne un résumé statistique du marché immobilier"
)
def get_market_summary():
    """
    Endpoint pour résumé rapide

    Utilisé par:
    - Dashboard principal
    - KPIs
    - Alertes
    """
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle non disponible"
        )

    try:
        trends = model.get_all_trends()

        # Calculer les statistiques
        rising = sum(1 for t in trends if t['trend_label'] == 'RISING')
        stable = sum(1 for t in trends if t['trend_label'] == 'STABLE')
        declining = sum(1 for t in trends if t['trend_label'] == 'DECLINING')

        avg_price = sum(t['current_avg_price_eth'] for t in trends) / len(trends)
        avg_predicted_3m = sum(t['predicted_price_3m_eth'] for t in trends) / len(trends)

        # Variation globale
        global_trend = ((avg_predicted_3m - avg_price) / avg_price) * 100

        return {
            "total_neighborhoods": len(trends),
            "avg_current_price_eth": round(avg_price, 4),
            "avg_predicted_price_3m_eth": round(avg_predicted_3m, 4),
            "global_trend_percentage": round(global_trend, 2),
            "market_sentiment": "BULLISH" if global_trend > 1 else "BEARISH" if global_trend < -1 else "NEUTRAL",
            "breakdown": {
                "rising": rising,
                "stable": stable,
                "declining": declining
            },
            "recommendation": "Good time to invest" if rising > declining else "Market correction expected" if declining > rising else "Wait and see"
        }

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors du calcul du résumé: {str(e)}"
        )


@router.get(
    "/health",
    summary="Vérifier le statut du modèle",
    description="Vérifie si le modèle Market Trend est chargé et opérationnel"
)
def health_check():
    """
    Health check spécifique au modèle Market Trend

    Utilisé par:
    - DevOps (monitoring)
    - Docker health checks
    """
    if model is None:
        return {
            "status": "DOWN",
            "model": "market_trend",
            "loaded": False,
            "message": "Modèle non chargé"
        }

    return {
        "status": "UP",
        "model": "market_trend",
        "loaded": True,
        "message": "Modèle opérationnel",
        "stats": {
            "n_clusters": model.n_clusters,
            "n_neighborhoods": model.n_neighborhoods,
            "optimized": model.optimized
        }
    }