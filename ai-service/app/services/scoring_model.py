"""
RISK SCORING MODEL (VERSION AVEC GRIDSEARCHCV)
==============================================
Ce module :
- entraîne un modèle de scoring locataire
- utilise GridSearchCV pour optimiser les hyperparamètres
- prédit un score de risque (0–100)
- sauvegarde le modèle pour l'API FastAPI
"""

import pandas as pd
import pickle
from pathlib import Path

from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, r2_score

# =========================
# PATHS
# =========================
DATA_PATH = Path("datasets/raw/tenant_risk.csv")
MODEL_PATH = Path("models/risk_scoring_model.pkl")

# =========================
# GRIDSEARCHCV CONFIGURATION
# =========================
USE_GRIDSEARCH = True  # Mettre à False pour entraînement rapide

PARAM_GRID = {
    'n_estimators': [50, 100, 200],
    'max_depth': [5, 10, 15, None],
    'min_samples_split': [2, 5, 10],
    'min_samples_leaf': [1, 2, 4],
    'max_features': ['sqrt', 'log2']
}

# =========================
# TRAINING FUNCTION
# =========================
def train_risk_model():
    print("=" * 60)
    print("🚀 ENTRAÎNEMENT DU MODÈLE RISK SCORING")
    if USE_GRIDSEARCH:
        print("🎯 MODE: GridSearchCV (Hyperparameter Tuning)")
    else:
        print("⚡ MODE: Entraînement rapide")
    print("=" * 60)

    # ===========================
    # 1️⃣ CHARGER LES DONNÉES
    # ===========================
    if not DATA_PATH.exists():
        raise FileNotFoundError(f"❌ Dataset introuvable: {DATA_PATH}")

    df = pd.read_csv(DATA_PATH)
    print(f"📊 Dataset chargé: {len(df)} tenants")

    # ===========================
    # 2️⃣ FEATURES (X) et TARGET (y)
    # ===========================
    X = df[[
        "income",
        "debt_ratio",
        "total_bookings",
        "cancellations",
        "late_cancellations",
        "avg_rating"
    ]]

    y = df["risk_score"]

    print(f"\n📈 Statistiques du risk_score:")
    print(f"   Min:  {y.min():.0f}")
    print(f"   Max:  {y.max():.0f}")
    print(f"   Mean: {y.mean():.2f}")
    print(f"   Std:  {y.std():.2f}")

    # ===========================
    # 3️⃣ TRAIN / TEST SPLIT
    # ===========================
    X_train, X_test, y_train, y_test = train_test_split(
        X, y,
        test_size=0.2,
        random_state=42
    )

    print(f"\n🔀 Split des données:")
    print(f"   Train: {len(X_train)} tenants")
    print(f"   Test:  {len(X_test)} tenants")

    # ===========================
    # 4️⃣ MODÈLE + GRIDSEARCHCV
    # ===========================

    if USE_GRIDSEARCH:
        print(f"\n🎯 GRIDSEARCHCV - RECHERCHE DES MEILLEURS HYPERPARAMÈTRES")
        print(f"   Paramètres à tester:")
        for param, values in PARAM_GRID.items():
            print(f"      {param}: {values}")

        total_combinations = 1
        for values in PARAM_GRID.values():
            total_combinations *= len(values)
        print(f"\n   Total de combinaisons: {total_combinations}")
        print(f"   Cross-validation: 5 folds")
        print(f"   Total d'entraînements: {total_combinations * 5}")
        print(f"\n   ⏳ Cela peut prendre 2-5 minutes...")

        # GridSearchCV
        base_model = RandomForestRegressor(random_state=42)

        grid_search = GridSearchCV(
            estimator=base_model,
            param_grid=PARAM_GRID,
            cv=5,
            scoring='neg_mean_absolute_error',
            n_jobs=-1,
            verbose=1
        )

        grid_search.fit(X_train, y_train)

        # Meilleurs paramètres
        print(f"\n✅ MEILLEURS HYPERPARAMÈTRES TROUVÉS:")
        for param, value in grid_search.best_params_.items():
            print(f"   {param}: {value}")

        print(f"\n📊 Meilleur score CV: {-grid_search.best_score_:.2f} MAE")

        model = grid_search.best_estimator_

    else:
        print(f"\n🤖 Entraînement du modèle avec paramètres par défaut...")

        model = RandomForestRegressor(
            n_estimators=100,
            random_state=42
        )

        model.fit(X_train, y_train)
        print("✅ Modèle entraîné!")

    # ===========================
    # 5️⃣ ÉVALUATION
    # ===========================
    print(f"\n📊 ÉVALUATION DU MODÈLE:")

    predictions = model.predict(X_test)
    mae = mean_absolute_error(y_test, predictions)
    r2 = r2_score(y_test, predictions)

    print(f"   MAE: {mae:.2f} points")
    print(f"   R² Score: {r2:.3f}")
    print(f"\n   Interprétation:")
    print(f"   - Erreur moyenne: ~{mae:.0f} points sur 100")
    print(f"   - Variance expliquée: {r2*100:.1f}%")

    # Feature importance
    feature_importance = pd.DataFrame({
        'feature': X.columns,
        'importance': model.feature_importances_
    }).sort_values('importance', ascending=False)

    print(f"\n🎯 Importance des features:")
    for idx, row in feature_importance.iterrows():
        print(f"   {row['feature']:20s} {row['importance']*100:5.1f}%")

    # ===========================
    # 6️⃣ SAUVEGARDE DU MODÈLE
    # ===========================
    MODEL_PATH.parent.mkdir(exist_ok=True)

    model_data = {
        'model': model,
        'feature_names': list(X.columns),
        'mae': mae,
        'r2': r2,
        'best_params': grid_search.best_params_ if USE_GRIDSEARCH else None
    }

    with open(MODEL_PATH, "wb") as f:
        pickle.dump(model_data, f)

    print(f"\n💾 Modèle sauvegardé dans: {MODEL_PATH}")

    # ===========================
    # 7️⃣ TEST RAPIDE
    # ===========================
    print(f"\n🧪 TEST RAPIDE:")
    test_tenant = {
        'income': 7000,
        'debt_ratio': 0.3,
        'total_bookings': 5,
        'cancellations': 1,
        'late_cancellations': 0,
        'avg_rating': 4.5
    }

    X_test_single = pd.DataFrame([test_tenant])
    predicted_risk = model.predict(X_test_single)[0]

    print(f"   Tenant test: {test_tenant}")
    print(f"   Risque prédit: {predicted_risk:.0f}/100")

    print("=" * 60)
    print("✅ ENTRAÎNEMENT TERMINÉ AVEC SUCCÈS!")
    print("=" * 60)

    return model


# =========================
# CLASSE POUR UTILISATION EN PROD
# =========================
class RiskScoringModel:
    """
    Wrapper pour charger et utiliser le modèle en production
    """

    def __init__(self):
        if not MODEL_PATH.exists():
            raise FileNotFoundError(
                f"❌ Modèle Risk Scoring introuvable: {MODEL_PATH}\n"
                f"   Lancez: python app/services/scoring_model.py"
            )

        with open(MODEL_PATH, "rb") as f:
            model_data = pickle.load(f)

        # Compatibilité avec ancien format
        if isinstance(model_data, dict):
            self.model = model_data['model']
            self.feature_names = model_data.get('feature_names', [])
            self.mae = model_data.get('mae', None)
            self.r2 = model_data.get('r2', None)
            self.best_params = model_data.get('best_params', None)
        else:
            self.model = model_data
            self.feature_names = []
            self.mae = None
            self.r2 = None
            self.best_params = None

        print(f"✅ Modèle Risk Scoring chargé depuis {MODEL_PATH}")
        if self.best_params:
            print(f"   🎯 Modèle optimisé avec GridSearchCV")

    def predict(self, data: dict) -> int:
        """
        Prédit le score de risque (0–100)
        """
        features = [[
            data["income"],
            data["debt_ratio"],
            data["total_bookings"],
            data["cancellations"],
            data["late_cancellations"],
            data["avg_rating"]
        ]]

        score = int(self.model.predict(features)[0])
        return min(max(score, 0), 100)


# =========================
# MAIN
# =========================
if __name__ == "__main__":
    train_risk_model()