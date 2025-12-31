"""
MARKET TREND MODEL
==================
Ce module :
- analyse les tendances de prix par quartier
- utilise time-series clustering pour détecter les patterns
- génère des prédictions d'évolution des prix
- sauvegarde le modèle pour l'API FastAPI

🎯 Time-series clustering pour prédire l'évolution du marché
📊 Heatmaps prédictives par quartier
"""

import pandas as pd
import numpy as np
import pickle
from pathlib import Path
from datetime import datetime, timedelta
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LinearRegression
from sklearn.metrics import silhouette_score, davies_bouldin_score

# =========================
# PATHS
# =========================
PROPERTY_DATA_PATH = Path("datasets/raw/property_price.csv")
MODEL_PATH = Path("models/market_trend_model.pkl")

# =========================
# CONFIGURATION
# =========================
N_NEIGHBORHOODS = 10  # Nombre de quartiers simulés
N_CLUSTERS = 3  # Nombre de clusters de tendances (rising, stable, declining)

USE_GRIDSEARCH = True  # Optimiser le nombre de clusters

# GridSearchCV configuration
CLUSTER_RANGE = [2, 3, 4, 5]  # Tester différents nombres de clusters

# =========================
# HELPER FUNCTIONS
# =========================

def generate_time_series_data(df_properties):
    """
    Génère des données de time-series pour chaque propriété

    Simule l'évolution des prix sur 12 mois
    """
    print("\n📅 Génération de données time-series...")

    time_series = []
    base_date = datetime.now() - timedelta(days=365)

    for idx, prop in df_properties.iterrows():
        property_id = prop['property_id']
        base_price = prop['price_per_night_eth']

        # Simuler une tendance (rising, stable, declining)
        trend_type = np.random.choice(['rising', 'stable', 'declining'], p=[0.4, 0.4, 0.2])

        # Générer 12 mois de données
        for month in range(12):
            date = base_date + timedelta(days=30 * month)

            # Évolution selon la tendance
            if trend_type == 'rising':
                growth = 1 + (month * 0.02)  # +2% par mois
            elif trend_type == 'declining':
                growth = 1 - (month * 0.01)  # -1% par mois
            else:
                growth = 1 + np.random.uniform(-0.01, 0.01)  # Stable ±1%

            # Ajouter du bruit
            noise = np.random.normal(0, 0.05)
            price = base_price * growth * (1 + noise)
            price = max(price, 0.01)  # Prix minimum

            time_series.append({
                'property_id': property_id,
                'date': date,
                'price_eth': round(price, 4),
                'trend_type': trend_type,
                'neighborhood': property_id % N_NEIGHBORHOODS  # Quartier simulé
            })

    df_time_series = pd.DataFrame(time_series)
    print(f"✅ {len(df_time_series)} points de données générés")

    return df_time_series


def extract_trend_features(df_time_series):
    """
    Extrait les features de tendance pour chaque propriété
    """
    print("\n🔍 Extraction des features de tendance...")

    features = []

    for prop_id in df_time_series['property_id'].unique():
        prop_data = df_time_series[df_time_series['property_id'] == prop_id].sort_values('date')

        prices = prop_data['price_eth'].values

        # Features statistiques
        mean_price = np.mean(prices)
        std_price = np.std(prices)
        min_price = np.min(prices)
        max_price = np.max(prices)

        # Tendance linéaire
        X = np.arange(len(prices)).reshape(-1, 1)
        y = prices
        lr = LinearRegression()
        lr.fit(X, y)
        slope = lr.coef_[0]  # Pente de la tendance

        # Volatilité
        returns = np.diff(prices) / prices[:-1]
        volatility = np.std(returns)

        features.append({
            'property_id': prop_id,
            'neighborhood': prop_data['neighborhood'].iloc[0],
            'mean_price': mean_price,
            'std_price': std_price,
            'price_range': max_price - min_price,
            'slope': slope,
            'volatility': volatility,
            'trend_strength': abs(slope) / mean_price  # Normalized slope
        })

    df_features = pd.DataFrame(features)
    print(f"✅ Features extraites pour {len(df_features)} propriétés")

    return df_features


# =========================
# TRAINING FUNCTION
# =========================

def train_trend_model():
    """
    Entraîne le modèle de Market Trend

    Étapes:
    1. Charger les données de propriétés
    2. Générer des time-series simulées
    3. Extraire les features de tendance
    4. Clustering KMeans pour identifier les patterns
    5. Calculer les prédictions par quartier
    6. Sauvegarder le modèle
    """
    print("=" * 60)
    print("🚀 ENTRAÎNEMENT DU MODÈLE MARKET TREND")
    print("=" * 60)

    # ===========================
    # 1️⃣ CHARGER LES DONNÉES
    # ===========================
    if not PROPERTY_DATA_PATH.exists():
        raise FileNotFoundError(f"❌ Dataset introuvable: {PROPERTY_DATA_PATH}")

    df_properties = pd.read_csv(PROPERTY_DATA_PATH)
    print(f"📊 Dataset chargé: {len(df_properties)} propriétés")

    # ===========================
    # 2️⃣ GÉNÉRER TIME-SERIES
    # ===========================
    df_time_series = generate_time_series_data(df_properties)

    # ===========================
    # 3️⃣ EXTRAIRE FEATURES
    # ===========================
    df_features = extract_trend_features(df_time_series)

    # ===========================
    # 4️⃣ CLUSTERING KMEANS + GRIDSEARCH
    # ===========================
    print(f"\n🤖 Clustering KMeans...")

    # Features pour le clustering
    feature_cols = ['mean_price', 'std_price', 'price_range', 'slope', 'volatility', 'trend_strength']
    X = df_features[feature_cols].values

    # Normalisation
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    if USE_GRIDSEARCH:
        print(f"\n🎯 OPTIMISATION DU NOMBRE DE CLUSTERS:")
        print(f"   Testant: {CLUSTER_RANGE} clusters")

        best_n_clusters = N_CLUSTERS
        best_score = -np.inf
        results = []

        for n_clusters in CLUSTER_RANGE:
            kmeans_test = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
            clusters_test = kmeans_test.fit_predict(X_scaled)

            # Métriques d'évaluation
            silhouette = silhouette_score(X_scaled, clusters_test)
            davies_bouldin = davies_bouldin_score(X_scaled, clusters_test)
            inertia = kmeans_test.inertia_

            results.append({
                'n_clusters': n_clusters,
                'silhouette': silhouette,
                'davies_bouldin': davies_bouldin,
                'inertia': inertia
            })

            print(f"\n   n_clusters={n_clusters}:")
            print(f"      Silhouette Score: {silhouette:.4f} (plus élevé = mieux)")
            print(f"      Davies-Bouldin: {davies_bouldin:.4f} (plus bas = mieux)")
            print(f"      Inertia: {inertia:.2f}")

            # Meilleur score = silhouette le plus élevé
            if silhouette > best_score:
                best_score = silhouette
                best_n_clusters = n_clusters

        print(f"\n✅ MEILLEUR NOMBRE DE CLUSTERS: {best_n_clusters}")
        print(f"   Silhouette Score: {best_score:.4f}")

        # Ré-entraîner avec le meilleur nombre
        kmeans = KMeans(n_clusters=best_n_clusters, random_state=42, n_init=10)
        clusters = kmeans.fit_predict(X_scaled)

        df_results = pd.DataFrame(results)

    else:
        # KMeans clustering standard
        kmeans = KMeans(n_clusters=N_CLUSTERS, random_state=42, n_init=10)
        clusters = kmeans.fit_predict(X_scaled)
        best_n_clusters = N_CLUSTERS
        df_results = None

    df_features['cluster'] = clusters

    print(f"\n✅ {best_n_clusters} clusters identifiés")

    # Interpréter les clusters
    cluster_info = []
    for cluster_id in range(best_n_clusters):
        cluster_data = df_features[df_features['cluster'] == cluster_id]
        avg_slope = cluster_data['slope'].mean()
        avg_volatility = cluster_data['volatility'].mean()
        avg_price = cluster_data['mean_price'].mean()

        # Déterminer le type de tendance
        if avg_slope > 0.001:
            trend_label = "RISING"
            description = "Prix en hausse"
        elif avg_slope < -0.001:
            trend_label = "DECLINING"
            description = "Prix en baisse"
        else:
            trend_label = "STABLE"
            description = "Prix stables"

        cluster_info.append({
            'cluster': cluster_id,
            'trend_label': trend_label,
            'description': description,
            'avg_slope': avg_slope,
            'avg_volatility': avg_volatility,
            'avg_price': avg_price,
            'count': len(cluster_data)
        })

    df_clusters = pd.DataFrame(cluster_info)

    print(f"\n📊 CLUSTERS IDENTIFIÉS:")
    for idx, row in df_clusters.iterrows():
        print(f"   Cluster {row['cluster']}: {row['trend_label']} - {row['description']}")
        print(f"      Prix moyen: {row['avg_price']:.4f} ETH")
        print(f"      Pente moyenne: {row['avg_slope']:.6f}")
        print(f"      Volatilité: {row['avg_volatility']:.4f}")
        print(f"      Propriétés: {row['count']}")

    # ===========================
    # 5️⃣ PRÉDICTIONS PAR QUARTIER
    # ===========================
    print(f"\n📈 Calcul des prédictions par quartier...")

    neighborhood_trends = []

    for neighborhood_id in range(N_NEIGHBORHOODS):
        neighborhood_data = df_features[df_features['neighborhood'] == neighborhood_id]

        if len(neighborhood_data) > 0:
            # Statistiques du quartier
            avg_price = neighborhood_data['mean_price'].mean()
            avg_slope = neighborhood_data['slope'].mean()
            avg_volatility = neighborhood_data['volatility'].mean()

            # Cluster dominant
            dominant_cluster = neighborhood_data['cluster'].mode()[0]
            cluster_label = df_clusters[df_clusters['cluster'] == dominant_cluster]['trend_label'].iloc[0]

            # Prédiction à 3 mois
            predicted_price_3m = avg_price * (1 + avg_slope * 3)

            # Prédiction à 6 mois
            predicted_price_6m = avg_price * (1 + avg_slope * 6)

            neighborhood_trends.append({
                'neighborhood_id': neighborhood_id,
                'current_avg_price_eth': round(avg_price, 4),
                'trend_label': cluster_label,
                'slope': avg_slope,
                'volatility': avg_volatility,
                'predicted_price_3m_eth': round(predicted_price_3m, 4),
                'predicted_price_6m_eth': round(predicted_price_6m, 4),
                'confidence': 'HIGH' if avg_volatility < 0.05 else 'MEDIUM' if avg_volatility < 0.1 else 'LOW'
            })

    df_neighborhood_trends = pd.DataFrame(neighborhood_trends)

    print(f"✅ Prédictions calculées pour {len(df_neighborhood_trends)} quartiers")

    # ===========================
    # 6️⃣ SAUVEGARDER LE MODÈLE
    # ===========================
    MODEL_PATH.parent.mkdir(exist_ok=True)

    model_data = {
        'kmeans': kmeans,
        'scaler': scaler,
        'cluster_info': df_clusters,
        'neighborhood_trends': df_neighborhood_trends,
        'time_series': df_time_series,
        'features': df_features,
        'n_clusters': best_n_clusters,
        'n_neighborhoods': N_NEIGHBORHOODS,
        'optimization_results': df_results,
        'optimized': USE_GRIDSEARCH
    }

    with open(MODEL_PATH, "wb") as f:
        pickle.dump(model_data, f)

    print(f"\n💾 Modèle sauvegardé dans: {MODEL_PATH}")

    # ===========================
    # 7️⃣ RÉSUMÉ
    # ===========================
    print(f"\n📊 RÉSUMÉ DES TENDANCES PAR QUARTIER:")
    for idx, row in df_neighborhood_trends.head(5).iterrows():
        print(f"\n   Quartier {row['neighborhood_id']}:")
        print(f"      Prix actuel: {row['current_avg_price_eth']:.4f} ETH")
        print(f"      Tendance: {row['trend_label']}")
        print(f"      Prédiction 3 mois: {row['predicted_price_3m_eth']:.4f} ETH")
        print(f"      Confiance: {row['confidence']}")

    print("=" * 60)
    print("✅ ENTRAÎNEMENT TERMINÉ AVEC SUCCÈS!")
    print("=" * 60)

    return model_data


# =========================
# CLASSE POUR UTILISATION EN PROD
# =========================

class MarketTrendModel:
    """
    Wrapper pour charger et utiliser le modèle en production
    """

    def __init__(self):
        if not MODEL_PATH.exists():
            raise FileNotFoundError(
                f"❌ Modèle Market Trend introuvable: {MODEL_PATH}\n"
                f"   Lancez: python app/services/trend_model.py"
            )

        with open(MODEL_PATH, "rb") as f:
            model_data = pickle.load(f)

        self.kmeans = model_data['kmeans']
        self.scaler = model_data['scaler']
        self.cluster_info = model_data['cluster_info']
        self.neighborhood_trends = model_data['neighborhood_trends']
        self.time_series = model_data['time_series']
        self.features = model_data['features']
        self.n_clusters = model_data['n_clusters']
        self.n_neighborhoods = model_data['n_neighborhoods']
        self.optimization_results = model_data.get('optimization_results', None)
        self.optimized = model_data.get('optimized', False)

        print(f"✅ Modèle Market Trend chargé depuis {MODEL_PATH}")
        if self.optimized:
            print(f"   🎯 Modèle optimisé ({self.n_clusters} clusters)")

    def get_neighborhood_trend(self, neighborhood_id: int) -> dict:
        """
        Obtient la tendance d'un quartier spécifique
        """
        trend = self.neighborhood_trends[
            self.neighborhood_trends['neighborhood_id'] == neighborhood_id
            ]

        if trend.empty:
            return None

        return trend.iloc[0].to_dict()

    def get_all_trends(self) -> list:
        """
        Obtient toutes les tendances par quartier
        """
        return self.neighborhood_trends.to_dict('records')

    def get_heatmap_data(self) -> dict:
        """
        Prépare les données pour une heatmap
        """
        heatmap = {
            'neighborhoods': [],
            'current_prices': [],
            'predicted_prices_3m': [],
            'trend_labels': []
        }

        for idx, row in self.neighborhood_trends.iterrows():
            heatmap['neighborhoods'].append(f"Quartier {row['neighborhood_id']}")
            heatmap['current_prices'].append(float(row['current_avg_price_eth']))
            heatmap['predicted_prices_3m'].append(float(row['predicted_price_3m_eth']))
            heatmap['trend_labels'].append(row['trend_label'])

        return heatmap


# =========================
# MAIN
# =========================

if __name__ == "__main__":
    train_trend_model()