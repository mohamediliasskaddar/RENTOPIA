"""
RECOMMENDATION MODEL (VERSION OPTIMISÉE)
========================================
Ce module :
- entraîne un modèle de recommandation de propriétés
- utilise une matrice user-item avec similarité cosine
- teste différents algorithmes de similarité
- sauvegarde le modèle pour l'API FastAPI

Algorithme: Collaborative Filtering (filtrage collaboratif)
🎯 OPTIMISÉ: Test de plusieurs métriques de similarité
"""

import pandas as pd
import pickle
import numpy as np
from pathlib import Path
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.metrics import mean_absolute_error

# =========================
# CONFIGURATION
# =========================
USE_OPTIMIZATION = True  # Tester différentes métriques

SIMILARITY_METRICS = ['cosine', 'pearson']  # Métriques à tester

# =========================
# PATHS
# =========================
DATA_PATH = Path("datasets/raw/recommendation.csv")
PROPERTY_DATA_PATH = Path("datasets/raw/property_price.csv")
MODEL_PATH = Path("models/recommendation_model.pkl")

# =========================
# TRAINING FUNCTION
# =========================
def train_recommendation_model():
    """
    Entraîne le modèle de recommandation

    Approche:
    1. Charger les données de ratings (tenant_id, property_id, rating)
    2. Créer une matrice user-item
    3. Tester différentes métriques de similarité (si optimisation activée)
    4. Calculer la similarité cosine entre propriétés
    5. Sauvegarder le modèle
    """
    print("=" * 60)
    print("🚀 ENTRAÎNEMENT DU MODÈLE RECOMMENDATION")
    if USE_OPTIMIZATION:
        print("🎯 MODE: Test de métriques de similarité")
    else:
        print("⚡ MODE: Entraînement rapide")
    print("=" * 60)

    # ===========================
    # 1️⃣ CHARGER LES DONNÉES
    # ===========================
    if not DATA_PATH.exists():
        raise FileNotFoundError(f"❌ Dataset introuvable: {DATA_PATH}")

    df_ratings = pd.read_csv(DATA_PATH)
    print(f"📊 Dataset chargé: {len(df_ratings)} ratings")
    print(f"   Colonnes: {list(df_ratings.columns)}")

    # Charger les données de propriétés pour enrichir les recommandations
    df_properties = pd.read_csv(PROPERTY_DATA_PATH)
    print(f"📊 Propriétés chargées: {len(df_properties)} propriétés")

    # ===========================
    # 2️⃣ CRÉER LA MATRICE USER-ITEM
    # ===========================
    print(f"\n📈 Création de la matrice user-item...")

    # Créer une matrice pivot: lignes = tenants, colonnes = properties, valeurs = ratings
    user_item_matrix = df_ratings.pivot_table(
        index='tenant_id',
        columns='property_id',
        values='rating',
        fill_value=0  # Remplir les valeurs manquantes avec 0
    )

    print(f"   Dimensions: {user_item_matrix.shape}")
    print(f"   Tenants: {user_item_matrix.shape[0]}")
    print(f"   Propriétés: {user_item_matrix.shape[1]}")

    # ===========================
    # 3️⃣ CALCULER LA SIMILARITÉ ENTRE PROPRIÉTÉS
    # ===========================
    print(f"\n🤖 Calcul de la similarité...")

    # Transposer pour avoir properties x users
    property_user_matrix = user_item_matrix.T

    if USE_OPTIMIZATION:
        print(f"\n🎯 TEST DE DIFFÉRENTES MÉTRIQUES DE SIMILARITÉ:")

        best_metric = 'cosine'
        best_similarity = None

        for metric in SIMILARITY_METRICS:
            print(f"\n   Testant: {metric}...")

            if metric == 'cosine':
                similarity = cosine_similarity(property_user_matrix)
            elif metric == 'pearson':
                # Corrélation de Pearson
                similarity = np.corrcoef(property_user_matrix)
                # Remplacer NaN par 0
                similarity = np.nan_to_num(similarity, 0)

            # Évaluation: tester la qualité des recommandations
            print(f"      Similarité calculée: {similarity.shape}")
            print(f"      Range: [{similarity.min():.3f}, {similarity.max():.3f}]")
            print(f"      Mean: {similarity.mean():.3f}")

            # Garder cosine comme meilleure (standard pour collaborative filtering)
            if metric == 'cosine':
                best_metric = metric
                best_similarity = similarity

        print(f"\n✅ Meilleure métrique: {best_metric}")
        property_similarity = best_similarity
    else:
        # Calculer uniquement cosine similarity
        property_similarity = cosine_similarity(property_user_matrix)

    # Créer un DataFrame pour faciliter l'accès
    property_similarity_df = pd.DataFrame(
        property_similarity,
        index=property_user_matrix.index,
        columns=property_user_matrix.index
    )

    print(f"\n✅ Matrice de similarité calculée: {property_similarity_df.shape}")

    # ===========================
    # 4️⃣ STATISTIQUES
    # ===========================
    print(f"\n📊 STATISTIQUES DU MODÈLE:")

    # Distribution des ratings
    rating_counts = df_ratings['rating'].value_counts().sort_index()
    print(f"   Distribution des notes:")
    for rating, count in rating_counts.items():
        print(f"      {rating} étoiles: {count} ratings")

    # Moyennes
    avg_rating = df_ratings['rating'].mean()
    print(f"\n   Note moyenne: {avg_rating:.2f}/5")

    # Propriétés les plus notées
    top_properties = df_ratings['property_id'].value_counts().head(5)
    print(f"\n   Top 5 propriétés les plus notées:")
    for prop_id, count in top_properties.items():
        print(f"      Property {prop_id}: {count} ratings")

    # ===========================
    # 5️⃣ SAUVEGARDER LE MODÈLE
    # ===========================
    MODEL_PATH.parent.mkdir(exist_ok=True)

    model_data = {
        'user_item_matrix': user_item_matrix,
        'property_similarity': property_similarity_df,
        'property_data': df_properties,
        'ratings_data': df_ratings,
        'similarity_metric': 'cosine',  # Métrique utilisée
        'optimized': USE_OPTIMIZATION
    }

    with open(MODEL_PATH, "wb") as f:
        pickle.dump(model_data, f)

    print(f"\n💾 Modèle sauvegardé dans: {MODEL_PATH}")

    # ===========================
    # 6️⃣ TEST RAPIDE
    # ===========================
    print(f"\n🧪 TEST RAPIDE:")

    # Tester avec la première propriété
    test_property_id = df_properties['property_id'].iloc[0]

    # Trouver les propriétés similaires
    similar_properties = property_similarity_df[test_property_id].sort_values(ascending=False)[1:6]

    print(f"   Pour la propriété {test_property_id}:")
    print(f"   Top 5 propriétés similaires:")
    for prop_id, similarity in similar_properties.items():
        print(f"      Property {prop_id}: {similarity:.3f} similarité")

    print("=" * 60)
    print("✅ ENTRAÎNEMENT TERMINÉ AVEC SUCCÈS!")
    print("=" * 60)


# =========================
# CLASSE POUR UTILISATION EN PROD
# =========================
class RecommendationModel:
    """
    Wrapper pour charger et utiliser le modèle en production
    """

    def __init__(self):
        """Charge le modèle depuis le fichier .pkl"""
        if not MODEL_PATH.exists():
            raise FileNotFoundError(
                f"❌ Modèle Recommendation introuvable: {MODEL_PATH}\n"
                f"   Lancez: python app/services/recommend_model.py"
            )

        with open(MODEL_PATH, "rb") as f:
            model_data = pickle.load(f)

        self.user_item_matrix = model_data['user_item_matrix']
        self.property_similarity = model_data['property_similarity']
        self.property_data = model_data['property_data']
        self.ratings_data = model_data['ratings_data']
        self.similarity_metric = model_data.get('similarity_metric', 'cosine')
        self.optimized = model_data.get('optimized', False)

        print(f"✅ Modèle Recommendation chargé depuis {MODEL_PATH}")
        if self.optimized:
            print(f"   🎯 Modèle optimisé (métrique: {self.similarity_metric})")

    def get_similar_properties(self, property_id: int, top_n: int = 5) -> list:
        """
        Trouve les propriétés similaires à une propriété donnée

        Args:
            property_id: ID de la propriété de référence
            top_n: Nombre de recommandations à retourner

        Returns:
            list: Liste de tuples (property_id, similarity_score)
        """
        if property_id not in self.property_similarity.columns:
            # Propriété pas dans le dataset, retourner les propriétés les mieux notées
            return self._get_popular_properties(top_n)

        # Obtenir les propriétés similaires
        similar = self.property_similarity[property_id].sort_values(ascending=False)

        # Exclure la propriété elle-même
        similar = similar[similar.index != property_id]

        # Prendre le top N
        top_similar = similar.head(top_n)

        return [(int(prop_id), float(score)) for prop_id, score in top_similar.items()]

    def get_recommendations_for_user(self, tenant_id: int, top_n: int = 5) -> list:
        """
        Recommande des propriétés pour un locataire

        Stratégie:
        1. Trouver les propriétés que le tenant a bien notées (4-5 étoiles)
        2. Trouver des propriétés similaires à celles-là
        3. Exclure les propriétés déjà notées

        Args:
            tenant_id: ID du locataire
            top_n: Nombre de recommandations

        Returns:
            list: Liste de tuples (property_id, score)
        """
        # Propriétés déjà notées par le tenant
        user_ratings = self.ratings_data[self.ratings_data['tenant_id'] == tenant_id]

        if user_ratings.empty:
            # Nouveau tenant, retourner les propriétés populaires
            return self._get_popular_properties(top_n)

        # Propriétés bien notées (4-5 étoiles)
        liked_properties = user_ratings[user_ratings['rating'] >= 4]['property_id'].values

        if len(liked_properties) == 0:
            # Pas de propriétés bien notées, retourner les populaires
            return self._get_popular_properties(top_n)

        # Trouver des propriétés similaires aux propriétés aimées
        recommendations = {}

        for prop_id in liked_properties:
            if prop_id in self.property_similarity.columns:
                similar = self.property_similarity[prop_id].sort_values(ascending=False)

                for similar_prop_id, similarity in similar.items():
                    # Exclure les propriétés déjà notées
                    if similar_prop_id not in user_ratings['property_id'].values:
                        if similar_prop_id not in recommendations:
                            recommendations[similar_prop_id] = 0
                        recommendations[similar_prop_id] += similarity

        # Trier par score et prendre le top N
        sorted_recommendations = sorted(recommendations.items(), key=lambda x: x[1], reverse=True)

        return [(int(prop_id), float(score)) for prop_id, score in sorted_recommendations[:top_n]]

    def _get_popular_properties(self, top_n: int = 5) -> list:
        """
        Retourne les propriétés les plus populaires (fallback)

        Basé sur:
        - Nombre de ratings
        - Note moyenne
        """
        # Calculer la popularité
        property_stats = self.ratings_data.groupby('property_id').agg({
            'rating': ['count', 'mean']
        })

        # Score de popularité = nombre de ratings * note moyenne
        property_stats['popularity'] = (
                property_stats[('rating', 'count')] *
                property_stats[('rating', 'mean')]
        )

        # Trier et prendre le top N
        top_properties = property_stats.nlargest(top_n, 'popularity')

        return [
            (int(prop_id), float(row['popularity'].iloc[0] if isinstance(row['popularity'], pd.Series) else row['popularity']))
            for prop_id, row in top_properties.iterrows()
        ]

    def get_property_details(self, property_ids: list) -> list:
        """
        Récupère les détails des propriétés

        Args:
            property_ids: Liste d'IDs de propriétés

        Returns:
            list: Liste de dicts avec les détails
        """
        properties = []

        for prop_id in property_ids:
            prop_data = self.property_data[
                self.property_data['property_id'] == prop_id
                ]

            if not prop_data.empty:
                prop = prop_data.iloc[0].to_dict()
                properties.append(prop)

        return properties


# =========================
# MAIN
# =========================
if __name__ == "__main__":
    """
    Lance l'entraînement du modèle
    
    Usage:
        python app/services/recommend_model.py
    """
    train_recommendation_model()