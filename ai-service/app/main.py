"""
=====================================================================
MAIN.PY - POINT D'ENTRÉE DU MICROSERVICE IA
=====================================================================

Ce fichier est le CŒUR de notre application FastAPI.
Il fait 5 choses principales:

1. Initialise FastAPI (créer l'application web)
2. Configure CORS (autoriser les appels depuis le frontend)
3. Enregistre les routes (les endpoints /price, /scoring, /recommend, /trend)
4. Gère le cycle de vie (startup/shutdown)
5. Lance le serveur Uvicorn

C'est LE fichier qui démarre tout!
=====================================================================
"""

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging
from pathlib import Path
from datetime import datetime

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Import des routers (API endpoints)
from app.routers import price, scoring, recommend, trend

# =========================
# GESTION DU CYCLE DE VIE
# =========================
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Gestion du cycle de vie de l'application.

    STARTUP:
    - Charger les modèles ML en mémoire une SEULE FOIS au démarrage
    - Vérifier que les datasets existent
    - Initialiser les ressources

    SHUTDOWN:
    - Libérer les ressources
    - Sauvegarder les logs
    """
    # ========== STARTUP ==========
    logger.info("=" * 60)
    logger.info("🚀 DÉMARRAGE DU AI SERVICE")
    logger.info("=" * 60)

    try:
        # Vérifier les datasets
        datasets_path = Path("datasets/raw")
        if datasets_path.exists():
            logger.info(f"✅ Datasets trouvés")
        else:
            logger.warning("⚠️  Datasets non trouvés. Exécutez download_datasets.py")

        # Vérifier les modèles
        models_path = Path("models")
        if models_path.exists():
            models = list(models_path.glob("*.pkl"))
            if models:
                logger.info(f"✅ {len(models)} modèle(s) trouvé(s)")
            else:
                logger.warning("⚠️  Aucun modèle entraîné. Lancez train_*.py")

        logger.info("✅ AI Service prêt!")
        logger.info("=" * 60)

    except Exception as e:
        logger.error(f"❌ Erreur au démarrage: {e}")

    yield  # L'application tourne ici

    # ========== SHUTDOWN ==========
    logger.info("🛑 Arrêt du AI Service...")

# =========================
# CRÉATION DE L'APPLICATION FASTAPI
# =========================
app = FastAPI(
    title="AI Service - Rental Platform",
    description="""
    🤖 **Microservice d'Intelligence Artificielle**
    
    ## Fonctionnalités
    
    ### 💰 Price Prediction
    Prédiction du prix optimal de location (ETH)
    
    ### ⚠️ Tenant Risk Scoring
    Évaluation du risque locataire (score 0-100)
    
    ### 🏠 Property Recommendations
    Recommandations personnalisées de propriétés
    
    ### 📈 Market Trend Dashboard
    Tendances de marché et prédictions par quartier
    """,
    version="1.0.0",
    lifespan=lifespan
)

# =========================
# MIDDLEWARE CORS
# =========================
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:4200",  # Angular frontend
        "http://localhost:8080",  # API Gateway
        "http://localhost:8081",  # Booking service
        "http://localhost:8089",  # Blockchain service
        "*"  # Autoriser tout (dev seulement!)
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# =========================
# ENDPOINT RACINE
# =========================
@app.get("/", tags=["Root"])
def root():
    """
    Endpoint racine - Informations sur le service
    """
    return {
        "service": "AI Service - Rental Platform",
        "version": "1.0.0",
        "status": "running",
        "timestamp": datetime.now().isoformat(),
        "documentation": "/docs",
        "endpoints": {
            "health": "/health",
            "price_prediction": "/price",
            "risk_scoring": "/scoring",
            "recommendations": "/recommend",
            "market_trend": "/trend"
        }
    }

# =========================
# ENDPOINT DE SANTÉ (Health Check)
# =========================
@app.get("/health", tags=["Health"])
def health_check():
    """
    """

    # Vérifier les datasets
    datasets_path = Path("datasets/raw")
    datasets_exist = datasets_path.exists()

    # Vérifier les modèles
    models_path = Path("models")
    models_count = len(list(models_path.glob("*.pkl"))) if models_path.exists() else 0

    return {
        "status": "UP",
        "service": "ai-service",
        "version": "1.0.0",
        "timestamp": datetime.now().isoformat(),
        "checks": {
            "datasets_available": datasets_exist,
            "models_trained": models_count > 0,
            "models_count": models_count
        },
        "ready": datasets_exist and models_count > 0
    }

# =========================
# ENREGISTREMENT DES ROUTERS
# =========================

app.include_router(
    price.router,
    prefix="/price",
    tags=["💰 Price Prediction"]
)

app.include_router(
    scoring.router,
    prefix="/scoring",
    tags=["⚠️ Risk Scoring"]
)

app.include_router(
    recommend.router,
    prefix="/recommend",
    tags=["🏠 Recommendations"]
)

app.include_router(
    trend.router,
    prefix="/trend",
    tags=["📈 Market Trend"]
)

# =========================
# LANCEMENT DU SERVEUR
# =========================
if __name__ == "__main__":
    """
    Point d'entrée pour lancer l'application
    
    Utilisation:
        python app/main.py
    
    L'application sera accessible sur:
        - http://localhost:8090
        - Documentation: http://localhost:8090/docs
    """

    print("=" * 60)
    print("🤖 AI SERVICE - RENTAL PLATFORM")
    print("=" * 60)
    print(f"📍 Host: 0.0.0.0")
    print(f"🔌 Port: 8090")
    print(f"📚 Docs: http://localhost:8090/docs")
    print(f"🏥 Health: http://localhost:8090/health")
    print("=" * 60)

    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8090,
        reload=True,
        log_level="info"
    )