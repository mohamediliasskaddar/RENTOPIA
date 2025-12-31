from fastapi import APIRouter, HTTPException
from app.schemas.risk import (
    RiskPredictionRequest,
    RiskPredictionResponse
)
from app.services.scoring_model import RiskScoringModel

router = APIRouter()
model = RiskScoringModel()

def risk_level(score: int) -> str:
    if score < 30:
        return "LOW"
    elif score < 70:
        return "MEDIUM"
    return "HIGH"


@router.post(
    "/predict",
    response_model=RiskPredictionResponse,
    summary="Prédire le risque d’un locataire"
)
def predict_risk(request: RiskPredictionRequest):
    """
    Endpoint appelé par le backend
    """
    try:
        score = model.predict(request.dict())
        return RiskPredictionResponse(
            risk_score=score,
            risk_level=risk_level(score)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
