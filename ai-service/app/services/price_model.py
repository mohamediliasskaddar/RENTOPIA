"""
PRICE PREDICTION MODEL (VERSION ETH + GRIDSEARCHCV)
===================================================
Ce module :
- entraîne un modèle de prédiction de prix EN ETH
- utilise GridSearchCV pour optimiser les hyperparamètres
- prédit le prix par nuit d'une propriété
- sauvegarde le modèle pour l'API FastAPI

🔥 BLOCKCHAIN-NATIVE: Prix directement en ETH
🎯 OPTIMISÉ: GridSearchCV pour meilleurs hyperparamètres
"""

import pandas as pd
import pickle
from pathlib import Path

from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.metrics import mean_absolute_error, r2_score

# =========================
# PATHS
# =========================
DATA_PATH = Path("datasets/raw/property_price.csv")
MODEL_PATH = Path("models/price_prediction_model.pkl")

# =========================
# GRIDSEARCHCV CONFIGURATION
# =========================
USE_GRIDSEARCH = True  # Mettre à False pour entraînement rapide

PARAM_GRID = {
    'n_estimators': [50, 100, 200],
    'learning_rate': [0.05, 0.1, 0.2],
    'max_depth': [3, 5, 7],
    'min_samples_split': [2, 5],
    'min_samples_leaf': [1, 2]
}

# =========================
# TRAINING FUNCTION
# =========================
def train_price_model():
    """
    Entraîne le modèle de prédiction de prix EN ETH

    Features utilisées:
    - surface (m²)
    - rooms (nombre de chambres)
    - amenities_count (nombre d'équipements)
    - avg_rating (note moyenne 1-5)
    - occupancy_rate (taux d'occupation 0-1)

    Target:
    - price_per_night_eth (prix par nuit en ETH)
    """
    print("=" * 60)
    print("🚀 ENTRAÎNEMENT DU MODÈLE PRICE PREDICTION (ETH)")
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
    print(f"📊 Dataset chargé: {len(df)} propriétés")
    print(f"   Colonnes: {list(df.columns)}")

    # ===========================
    # 2️⃣ FEATURES (X) et TARGET (y)
    # ===========================
    X = df[[
        "surface",
        "rooms",
        "amenities_count",
        "avg_rating",
        "occupancy_rate"
    ]]

    y = df["price_per_night_eth"]

    print(f"\n📈 Statistiques du prix (ETH):")
    print(f"   Min:  {y.min():.4f} ETH")
    print(f"   Max:  {y.max():.4f} ETH")
    print(f"   Mean: {y.mean():.4f} ETH")
    print(f"   Std:  {y.std():.4f} ETH")

    # ===========================
    # 3️⃣ TRAIN / TEST SPLIT
    # ===========================
    X_train, X_test, y_train, y_test = train_test_split(
        X, y,
        test_size=0.2,
        random_state=42
    )

    print(f"\n🔀 Split des données:")
    print(f"   Train: {len(X_train)} propriétés")
    print(f"   Test:  {len(X_test)} propriétés")

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
        base_model = GradientBoostingRegressor(random_state=42)

        grid_search = GridSearchCV(
            estimator=base_model,
            param_grid=PARAM_GRID,
            cv=5,
            scoring='neg_mean_absolute_error',
            n_jobs=-1,  # Utiliser tous les CPU
            verbose=1
        )

        grid_search.fit(X_train, y_train)

        # Meilleurs paramètres
        print(f"\n✅ MEILLEURS HYPERPARAMÈTRES TROUVÉS:")
        for param, value in grid_search.best_params_.items():
            print(f"   {param}: {value}")

        print(f"\n📊 Meilleur score CV: {-grid_search.best_score_:.4f} MAE (ETH)")

        # Utiliser le meilleur modèle
        model = grid_search.best_estimator_

    else:
        print(f"\n🤖 Entraînement du modèle avec paramètres par défaut...")

        model = GradientBoostingRegressor(
            n_estimators=100,
            learning_rate=0.1,
            max_depth=5,
            random_state=42,
            verbose=0
        )

        model.fit(X_train, y_train)
        print("✅ Modèle entraîné!")

    # ===========================
    # 5️⃣ ÉVALUATION
    # ===========================
    print(f"\n📊 ÉVALUATION DU MODÈLE:")

    y_pred = model.predict(X_test)

    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)

    ETH_EUR_RATE = 3500.0
    mae_eur = mae * ETH_EUR_RATE

    print(f"   MAE (ETH):  {mae:.4f} ETH")
    print(f"   MAE (EUR):  {mae_eur:.2f} EUR")
    print(f"   R² Score:   {r2:.3f}")
    print(f"\n   Interprétation:")
    print(f"   - Erreur moyenne: ~{mae_eur:.0f}€ par nuit")
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
    # 6️⃣ SAUVEGARDER LE MODÈLE
    # ===========================
    MODEL_PATH.parent.mkdir(exist_ok=True)

    # Sauvegarder le modèle + métadonnées
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
    test_property = {
        'surface': 85,
        'rooms': 3,
        'amenities_count': 8,
        'avg_rating': 4.4,
        'occupancy_rate': 0.72
    }

    X_test_single = pd.DataFrame([test_property])
    predicted_price_eth = model.predict(X_test_single)[0]
    predicted_price_eur = predicted_price_eth * ETH_EUR_RATE

    print(f"   Propriété test: {test_property}")
    print(f"   Prix prédit:    {predicted_price_eth:.4f} ETH (~{predicted_price_eur:.0f}€)")

    print("=" * 60)
    print("✅ ENTRAÎNEMENT TERMINÉ AVEC SUCCÈS!")
    print("=" * 60)

    return model


# =========================
# CLASSE POUR UTILISATION EN PROD
# =========================
class PricePredictionModel:
    """
    Wrapper pour charger et utiliser le modèle en production
    """

    def __init__(self):
        """Charge le modèle depuis le fichier .pkl"""
        if not MODEL_PATH.exists():
            raise FileNotFoundError(
                f"❌ Modèle Price Prediction introuvable: {MODEL_PATH}\n"
                f"   Lancez: python app/services/price_model.py"
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

        print(f"✅ Modèle Price Prediction chargé depuis {MODEL_PATH}")
        if self.best_params:
            print(f"   🎯 Modèle optimisé avec GridSearchCV")

    def predict(self, data: dict) -> float:
        """
        Prédit le prix par nuit EN ETH
        """
        features = [[
            data["surface"],
            data["rooms"],
            data["amenities_count"],
            data["avg_rating"],
            data["occupancy_rate"]
        ]]

        price_eth = self.model.predict(features)[0]
        return round(price_eth, 4)

    def predict_with_confidence(self, data: dict, confidence_margin: float = 0.1) -> dict:
        """
        Prédit le prix avec une fourchette de confiance
        """
        price = self.predict(data)

        margin = price * confidence_margin
        price_min = round(price - margin, 4)
        price_max = round(price + margin, 4)

        return {
            "price_eth": price,
            "confidence_range": {
                "min": price_min,
                "max": price_max
            }
        }


# =========================
# MAIN
# =========================
if __name__ == "__main__":
    """
    Lance l'entraînement du modèle
    
    Usage:
        python app/services/price_model.py
    """
    train_price_model()
