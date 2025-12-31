"""
Génération de datasets IA réalistes
Basés sur la logique métier + smart contract
VERSION BLOCKCHAIN-NATIVE (ETH)
"""

import random
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

# ===============================
# CONFIGURATION GLOBALE
# ===============================

NB_USERS = 1000
NB_PROPERTIES = 300
NB_BOOKINGS = 5000

OUTPUT_RAW = "datasets/raw/"

# 🔥 NOUVEAU: Taux de conversion ETH/EUR
ETH_EUR_RATE = 3500.0  # 1 ETH ≈ 3500 EUR (au 25/12/2024)

random.seed(42)
np.random.seed(42)

# ===============================
# UTILS
# ===============================

def random_date(start, end):
    """Génère une date aléatoire entre start et end"""
    delta = end - start
    return start + timedelta(days=random.randint(0, delta.days))


def eur_to_eth(price_eur: float) -> float:
    """
    Convertit un prix EUR en ETH
    Arrondi à 4 décimales (standard blockchain)
    """
    return round(price_eur / ETH_EUR_RATE, 4)


# ===============================
# 1️⃣ TENANT RISK DATASET
# Ce dataset sert à :
# - prédire le risque locataire
# - refuser / accepter une réservation
# - prioriser certains profils
# - ajuster la caution
# ===============================

def generate_tenant_risk():
    """
    Génère le dataset des risques locataires

    Features:
    - income: revenu mensuel (EUR)
    - debt_ratio: ratio d'endettement (0-1)
    - total_bookings: nombre total de réservations
    - cancellations: nombre d'annulations
    - late_cancellations: annulations tardives (pénalisées par smart contract)
    - avg_rating: note moyenne (1-5)

    Target:
    - risk_score: score de risque (0-100)
    """
    rows = []

    for tenant_id in range(1, NB_USERS + 1):
        total_bookings = random.randint(1, 20)
        cancellations = random.randint(0, total_bookings // 2)

        # Late cancellations = annulations pénalisantes
        # Augmentent le risque selon le smart contract
        late_cancellations = random.randint(0, cancellations)

        # Un locataire mal noté = plus risqué
        avg_rating = round(random.uniform(2.5, 5.0), 2)

        income = int(np.random.normal(9000, 2500))

        # Dette élevée = risque financier
        debt_ratio = round(random.uniform(0.1, 0.7), 2)

        # 🎯 SCORE basé sur logique métier + smart contract
        risk_score = (
                cancellations * 10 +
                late_cancellations * 15 +  # Poids plus élevé
                (5 - avg_rating) * 10 +
                debt_ratio * 50
        )

        # Normalisation (0–100)
        risk_score = min(int(risk_score), 100)

        rows.append([
            tenant_id,
            income,
            debt_ratio,
            total_bookings,
            cancellations,
            late_cancellations,
            avg_rating,
            risk_score
        ])

    df = pd.DataFrame(rows, columns=[
        "tenant_id",
        "income",
        "debt_ratio",
        "total_bookings",
        "cancellations",
        "late_cancellations",
        "avg_rating",
        "risk_score"
    ])

    df.to_csv(f"{OUTPUT_RAW}tenant_risk.csv", index=False)
    print(f"✅ tenant_risk.csv généré ({len(df)} lignes)")


# ===============================
# 2️⃣ PROPERTY PRICE DATASET (VERSION ETH)
# Sert à :
# - prédire le prix par nuit EN ETH
# - suggérer des prix dynamiques
# - comparer logements
# ===============================

def generate_property_price():
    """
    Génère le dataset des prix de propriétés

    🔥 NOUVEAU: Prix en EUR ET ETH

    Features:
    - surface: superficie (m²)
    - rooms: nombre de chambres
    - amenities_count: nombre d'équipements
    - avg_rating: note moyenne (1-5)
    - occupancy_rate: taux d'occupation (0-1)

    Targets:
    - price_per_night_eur: prix par nuit en EUR
    - price_per_night_eth: prix par nuit en ETH (🔥 NOUVEAU)
    """
    rows = []

    for property_id in range(1, NB_PROPERTIES + 1):
        surface = random.randint(30, 200)
        rooms = random.randint(1, 5)
        amenities = random.randint(3, 15)
        avg_rating = round(random.uniform(3, 5), 2)
        occupancy = round(random.uniform(0.3, 0.9), 2)

        # 🎯 Calcul du prix de base
        base_price = surface * 3 + rooms * 20 + amenities * 5

        # Logique métier:
        # - Forte demande (occupancy) → prix ↑
        # - Bon rating → prix ↑
        price_eur = base_price * (1 + occupancy) * (avg_rating / 4)
        price_eur = int(price_eur)

        # 🔥 CONVERSION EN ETH
        price_eth = eur_to_eth(price_eur)

        rows.append([
            property_id,
            surface,
            rooms,
            amenities,
            avg_rating,
            occupancy,
            price_eur,
            price_eth  # 🔥 NOUVELLE COLONNE
        ])

    df = pd.DataFrame(rows, columns=[
        "property_id",
        "surface",
        "rooms",
        "amenities_count",
        "avg_rating",
        "occupancy_rate",
        "price_per_night_eur",
        "price_per_night_eth"  # 🔥 NOUVELLE COLONNE
    ])

    df.to_csv(f"{OUTPUT_RAW}property_price.csv", index=False)
    print(f"✅ property_price.csv généré ({len(df)} lignes)")
    print(f"   💰 Prix ETH range: {df['price_per_night_eth'].min():.4f} - {df['price_per_night_eth'].max():.4f} ETH")


# ===============================
# 3️⃣ RECOMMENDATION DATASET
# Sert à :
# - recommander des propriétés aux locataires
# - système de matching
# ===============================

def generate_recommendation():
    """
    Génère le dataset des recommandations

    Structure:
    - tenant_id: identifiant du locataire
    - property_id: identifiant de la propriété
    - rating: note donnée par le locataire (1-5)
    """
    rows = []

    for _ in range(NB_BOOKINGS):
        tenant_id = random.randint(1, NB_USERS)
        property_id = random.randint(1, NB_PROPERTIES)
        rating = random.randint(1, 5)

        rows.append([
            tenant_id,
            property_id,
            rating
        ])

    df = pd.DataFrame(rows, columns=[
        "tenant_id",
        "property_id",
        "rating"
    ])

    df.to_csv(f"{OUTPUT_RAW}recommendation.csv", index=False)
    print(f"✅ recommendation.csv généré ({len(df)} lignes)")


# ===============================
# MAIN
# ===============================

if __name__ == "__main__":
    print("=" * 60)
    print("🚀 GÉNÉRATION DES DATASETS IA (VERSION BLOCKCHAIN-NATIVE)")
    print("=" * 60)
    print(f"📊 Configuration:")
    print(f"   - Utilisateurs: {NB_USERS}")
    print(f"   - Propriétés: {NB_PROPERTIES}")
    print(f"   - Réservations: {NB_BOOKINGS}")
    print(f"   - Taux ETH/EUR: 1 ETH = {ETH_EUR_RATE} EUR")
    print("=" * 60)

    generate_tenant_risk()
    generate_property_price()
    generate_recommendation()

    print("=" * 60)
    print("✅ TOUS LES DATASETS GÉNÉRÉS AVEC SUCCÈS!")
    print("=" * 60)