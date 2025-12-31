from pydantic import BaseModel, Field

class RiskPredictionRequest(BaseModel):
    """
    Données envoyées par le backend pour prédire le risque d’un locataire
    """
    income: float = Field(..., gt=0, description="Revenu mensuel du locataire")
    debt_ratio: float = Field(..., ge=0, le=1, description="Ratio d’endettement (0-1)")
    total_bookings: int = Field(..., ge=0)
    cancellations: int = Field(..., ge=0)
    late_cancellations: int = Field(..., ge=0)
    avg_rating: float = Field(..., ge=1, le=5)


class RiskPredictionResponse(BaseModel):
    """
    Réponse retournée au backend
    """
    risk_score: int
    risk_level: str
