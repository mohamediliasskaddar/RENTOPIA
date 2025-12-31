"""
ROUTER RECOMMENDATIONS
======================
Endpoints pour les recommandations de propriétés
"""

from fastapi import APIRouter, HTTPException, Query
from typing import Optional
from app.schemas.recommendation import (
    RecommendationRequest,
    RecommendationResponse,
    PropertyRecommendation,
    create_recommendation_response
)
from app.services.recommend_model import RecommendationModel

# =========================
# ROUTER SETUP
# =========================
router = APIRouter()

# Charger le modèle au démarrage du router
try:
    model = RecommendationModel()
except FileNotFoundError as e:
    print(f"⚠️  {e}")
    model = None


# =========================
# ENDPOINTS
# =========================

@router.post(
    "/predict",
    response_model=RecommendationResponse,
    summary="Obtenir des recommandations de propriétés",
    description="""
    Recommande des propriétés basées sur:
    - **tenant_id**: Recommandations personnalisées pour un locataire
    - **property_id**: Propriétés similaires à une propriété donnée
    - **Aucun des deux**: Propriétés les plus populaires
    
    **Algorithme**: Collaborative Filtering avec similarité cosine
    """
)
def get_recommendations(request: RecommendationRequest):
    """
    Endpoint principal pour les recommandations

    Utilisé par:
    - Frontend (afficher des suggestions)
    - Backend (booking-service)
    """
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle Recommendation non disponible. Lancez: python app/services/recommend_model.py"
        )

    try:
        recommendations_list = []
        rec_type = "popular"

        # Cas 1: Recommandations pour un tenant
        if request.tenant_id is not None:
            similar_props = model.get_recommendations_for_user(
                request.tenant_id,
                request.top_n
            )
            rec_type = "user-based"

        # Cas 2: Propriétés similaires
        elif request.property_id is not None:
            similar_props = model.get_similar_properties(
                request.property_id,
                request.top_n
            )
            rec_type = "item-based"

        # Cas 3: Propriétés populaires
        else:
            similar_props = model._get_popular_properties(request.top_n)
            rec_type = "popular"

        # Récupérer les détails des propriétés
        property_ids = [prop_id for prop_id, score in similar_props]
        property_details = model.get_property_details(property_ids)

        # Créer les objets PropertyRecommendation
        for (prop_id, score), details in zip(similar_props, property_details):
            recommendations_list.append(
                PropertyRecommendation(
                    property_id=int(prop_id),
                    score=round(float(score), 3),
                    surface=details.get('surface'),
                    rooms=details.get('rooms'),
                    amenities_count=details.get('amenities_count'),
                    avg_rating=details.get('avg_rating'),
                    occupancy_rate=details.get('occupancy_rate'),
                    price_per_night_eur=details.get('price_per_night_eur'),
                    price_per_night_eth=details.get('price_per_night_eth')
                )
            )

        # Créer la réponse
        return create_recommendation_response(
            recommendations=recommendations_list,
            recommendation_type=rec_type
        )

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la recommandation: {str(e)}"
        )


@router.get(
    "/properties",
    response_model=RecommendationResponse,
    summary="Recommandations via query parameters",
    description="Alternative GET pour obtenir des recommandations"
)
def get_recommendations_query(
        tenant_id: Optional[int] = Query(None, description="ID du locataire"),
        property_id: Optional[int] = Query(None, description="ID de la propriété"),
        top_n: int = Query(5, ge=1, le=20, description="Nombre de recommandations")
):
    """
    Endpoint GET alternatif pour les recommandations

    Plus facile à tester dans le navigateur
    """
    request = RecommendationRequest(
        tenant_id=tenant_id,
        property_id=property_id,
        top_n=top_n
    )

    return get_recommendations(request)


@router.get(
    "/example",
    response_model=RecommendationResponse,
    summary="Exemple de recommandations",
    description="Retourne des recommandations exemple (tenant_id=1)"
)
def get_example_recommendations():
    """
    Endpoint de démonstration

    Utile pour:
    - Tester rapidement l'API
    - Démo en soutenance
    """
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Modèle non disponible"
        )

    # Recommandations pour le tenant #1
    example_request = RecommendationRequest(
        tenant_id=1,
        property_id=None,
        top_n=5
    )

    return get_recommendations(example_request)


@router.get(
    "/health",
    summary="Vérifier le statut du modèle",
    description="Vérifie si le modèle Recommendation est chargé et opérationnel"
)
def health_check():
    """
    Health check spécifique au modèle Recommendation

    Utilisé par:
    - DevOps (monitoring)
    - Docker health checks
    """
    if model is None:
        return {
            "status": "DOWN",
            "model": "recommendation",
            "loaded": False,
            "message": "Modèle non chargé"
        }

    return {
        "status": "UP",
        "model": "recommendation",
        "loaded": True,
        "message": "Modèle opérationnel",
        "stats": {
            "total_properties": len(model.property_data),
            "total_ratings": len(model.ratings_data)
        }
    }