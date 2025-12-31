# 🤖 AI SERVICE - Microservice d'Intelligence Artificielle

> Microservice FastAPI avec 4 modèles ML optimisés pour la plateforme de location immobilière

---

## 📋 Table des Matières

1. [Vue d'ensemble](#-vue-densemble)
2. [Architecture](#-architecture)
3. [Modèles ML](#-modèles-ml)
4. [API Endpoints](#-api-endpoints)
5. [Installation & Démarrage](#-installation--démarrage)
6. [Intégration Frontend](#-intégration-frontend-angular)
7. [Datasets](#-datasets)
8. [Performance](#-performance)
9. [Documentation](#-documentation)

---

## 🎯 Vue d'ensemble

Le **AI Service** est un microservice Python/FastAPI qui fournit 4 fonctionnalités d'intelligence artificielle pour optimiser la plateforme de location :

| Fonctionnalité | Modèle | Optimisation | Objectif |
|----------------|--------|--------------|----------|
| **💰 Price Prediction** | GradientBoosting | GridSearchCV (108 combinaisons) | Suggérer le prix optimal en ETH |
| **⚠️ Risk Scoring** | RandomForest | GridSearchCV (216 combinaisons) | Évaluer le risque locataire (0-100) |
| **🏠 Recommendations** | Collaborative Filtering | Test métriques similarité | Recommander des propriétés |
| **📈 Market Trend** | KMeans Clustering | Optimisation clusters | Prédire les tendances de marché |

### 🏆 Points Forts

- ✅ **Blockchain-native** : Prix en ETH + EUR
- ✅ **Production-ready** : CORS configuré, health checks, validation Pydantic
- ✅ **Optimisé** : GridSearchCV sur tous les modèles
- ✅ **Documenté** : Swagger UI auto-générée
- ✅ **Performant** : Prédictions < 50ms
- ✅ **Scalable** : Architecture microservices

---

## 🏗️ Architecture
```
┌──────────────────────────────────────────────────────────┐
│                    FRONTEND (Angular)                     │
│                    Port: 4200                             │
└─────────────────────┬────────────────────────────────────┘
                      │ HTTP/JSON
                      │
┌─────────────────────▼────────────────────────────────────┐
│                 AI SERVICE (FastAPI)                      │
│                 Port: 8090                                │
│                                                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Price        │  │ Risk         │  │ Recommend    │   │
│  │ Prediction   │  │ Scoring      │  │ System       │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Market       │  │ ML Models    │  │ Datasets     │   │
│  │ Trend        │  │ (.pkl)       │  │ (CSV)        │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└────────────────────────────────────────────────────────────┘
```

### 🔧 Stack Technique

- **Backend**: FastAPI 0.104
- **ML**: scikit-learn 1.3, pandas, numpy
- **Visualisation**: plotly, seaborn, matplotlib (notebooks)
- **Validation**: Pydantic v2
- **Documentation**: Swagger UI 
---

## 🤖 Modèles ML

### 1️⃣ Price Prediction

**Objectif** : Prédire le prix optimal par nuit d'une propriété

| Métrique | Valeur |
|----------|--------|
| **Algorithme** | GradientBoostingRegressor |
| **Optimisation** | GridSearchCV (108 combinaisons) |
| **MAE** | 0.0106 ETH (~37€) |
| **R² Score** | 97.4% |
| **Features** | surface, rooms, amenities_count, avg_rating, occupancy_rate |

**Features Importantes** :
- 🏠 Surface : 74.5% (facteur principal)
- ⭐ Rating : 15.1%
- 📊 Occupancy : 7.6%

---

### 2️⃣ Risk Scoring

**Objectif** : Évaluer le risque d'un locataire (score 0-100)

| Métrique | Valeur |
|----------|--------|
| **Algorithme** | RandomForestRegressor |
| **Optimisation** | GridSearchCV (216 combinaisons) |
| **MAE** | 2.47 points |
| **R² Score** | 98.5% |
| **Features** | income, debt_ratio, total_bookings, cancellations, late_cancellations, avg_rating |

**Features Importantes** :
- 🚫 Cancellations : 49.2% (facteur principal)
- ⏰ Late cancellations : 30.2%
- 💳 Debt ratio : 6.9%

---

### 3️⃣ Recommendations

**Objectif** : Recommander des propriétés personnalisées

| Métrique | Valeur |
|----------|--------|
| **Algorithme** | Collaborative Filtering + Cosine Similarity |
| **Optimisation** | Test métriques (cosine > pearson) |
| **Matrice** | 991 tenants × 300 propriétés |
| **Similarité** | Cosine (meilleure que Pearson) |

**3 Types de Recommandations** :
- 👤 **User-based** : Basé sur l'historique du tenant
- 🏠 **Item-based** : Propriétés similaires à une sélection
- 🔥 **Popular** : Propriétés les plus populaires

---

### 4️⃣ Market Trend

**Objectif** : Prédire les tendances de prix par quartier

| Métrique | Valeur |
|----------|--------|
| **Algorithme** | KMeans Clustering sur time-series |
| **Optimisation** | Silhouette score (2 clusters optimaux) |
| **Clusters** | STABLE (78%) vs RISING (22%) |
| **Quartiers** | 10 quartiers analysés |

**Clusters Identifiés** :
- 📊 **STABLE** : 236 propriétés, prix moyen 0.1895 ETH
- 📈 **RISING** : 64 propriétés, prix moyen 0.3087 ETH

---

## 🚀 API Endpoints

### Base URL
```
http://localhost:8090
```

### 📚 Documentation Interactive
- **Swagger UI** : http://localhost:8090/docs

---

## 💰 Price Prediction

### `POST /price/predict`
Prédire le prix d'une propriété

**Request Body** :
```json
{
  "surface": 85,
  "rooms": 3,
  "amenities_count": 8,
  "avg_rating": 4.4,
  "occupancy_rate": 0.72
}
```

**Response** :
```json
{
  "predicted_price_eth": 0.2006,
  "confidence_range_eth": {
    "min": 0.1805,
    "max": 0.2207
  },
  "predicted_price_eur": 702,
  "confidence_range_eur": {
    "min": 631,
    "max": 772
  },
  "eth_eur_rate": 3500.0,
  "recommendation": "Prix haut de gamme - Propriété d'exception"
}
```

**Validation** :
- `surface` : 20-250 m²
- `rooms` : 1-10
- `amenities_count` : 0-20
- `avg_rating` : 0-5
- `occupancy_rate` : 0-1

### `GET /price/example`
Exemple de prédiction (85m², 3 chambres)

### `GET /price/health`
Vérifier le statut du modèle

---

## ⚠️ Risk Scoring

### `POST /scoring/predict`
Évaluer le risque d'un locataire

**Request Body** :
```json
{
  "income": 7000,
  "debt_ratio": 0.3,
  "total_bookings": 5,
  "cancellations": 1,
  "late_cancellations": 0,
  "avg_rating": 4.5
}
```

**Response** :
```json
{
  "risk_score": 32,
  "risk_level": "LOW",
  "trust_score": 68,
  "recommendation": "Locataire fiable - Risque faible",
  "factors": {
    "income_stability": "high",
    "payment_history": "good",
    "cancellation_rate": "low"
  }
}
```

**Risk Levels** :
- **LOW** : 0-33 (risque faible ✅)
- **MEDIUM** : 34-66 (risque modéré ⚠️)
- **HIGH** : 67-100 (risque élevé ❌)

**Validation** :
- `income` : 0-50000 €
- `debt_ratio` : 0-1
- `total_bookings` : 0-100
- `cancellations` : 0-50
- `avg_rating` : 0-5

---

## 🏠 Recommendations

### `POST /recommend/predict`
Obtenir des recommandations de propriétés

**Request Body (User-based)** :
```json
{
  "tenant_id": 1,
  "property_id": null,
  "top_n": 5
}
```

**Request Body (Item-based)** :
```json
{
  "tenant_id": null,
  "property_id": 76,
  "top_n": 5
}
```

**Request Body (Popular)** :
```json
{
  "tenant_id": null,
  "property_id": null,
  "top_n": 5
}
```

**Response** :
```json
{
  "recommendations": [
    {
      "property_id": 76,
      "score": 98,
      "surface": 193,
      "rooms": 3,
      "amenities_count": 5,
      "avg_rating": 4.56,
      "occupancy_rate": 0.36,
      "price_per_night_eur": 1029,
      "price_per_night_eth": 0.294
    }
  ],
  "count": 5,
  "recommendation_type": "user-based",
  "message": "Recommandations basées sur vos préférences"
}
```

**Types** :
- `user-based` : Recommandations personnalisées pour un tenant
- `item-based` : Propriétés similaires à une sélection
- `popular` : Propriétés les plus populaires

### `GET /recommend/properties?tenant_id=1&top_n=5`
Recommandations via GET (alternative)

### `GET /recommend/health`
Vérifier le statut du modèle

---

## 📈 Market Trend

### `GET /trend/trends`
Toutes les tendances de marché

**Response** :
```json
{
  "trends": [
    {
      "neighborhood_id": 0,
      "neighborhood_name": "Quartier 0",
      "current_price_eth": 0.2297,
      "current_price_eur": 804,
      "trend": "STABLE",
      "prediction_3m_eth": 0.2300,
      "prediction_3m_eur": 805,
      "confidence": "MEDIUM",
      "cluster": 0
    }
  ],
  "total": 10,
  "market_summary": {
    "avg_price_eth": 0.2076,
    "rising_neighborhoods": 3,
    "stable_neighborhoods": 7
  }
}
```

### `GET /trend/trends/{neighborhood_id}`
Tendance d'un quartier spécifique

### `GET /trend/heatmap`
Données pour heatmap (visualisation)

**Response** :
```json
{
  "neighborhoods": ["Quartier 0", "Quartier 1", ...],
  "prices_eth": [0.2297, 0.2086, ...],
  "prices_eur": [804, 730, ...],
  "trends": ["STABLE", "RISING", ...]
}
```

### `GET /trend/summary`
Résumé du marché

**Response** :
```json
{
  "total_neighborhoods": 10,
  "avg_price_eth": 0.2076,
  "avg_price_eur": 727,
  "clusters": {
    "STABLE": 7,
    "RISING": 3
  },
  "market_status": "stable_with_growth_pockets"
}
```

### `GET /trend/health`
Vérifier le statut du modèle

---

## 🔧 Installation & Démarrage

### Prérequis
- Python 3.11+
- pip

### Installation
```bash
# 1. Cloner le repository
cd ai-service

# 2. Créer l'environnement virtuel
python -m venv venv

# 3. Activer l'environnement
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 4. Installer les dépendances
pip install -r requirements.txt

# 5. Générer les datasets (première fois)
python datasets/generate_datasets.py

# 6. Entraîner les modèles (première fois)
python app/services/price_model.py
python app/services/scoring_model.py
python app/services/recommend_model.py
python app/services/trend_model.py
```

### Démarrage du Serveur
```bash
# Lancer le serveur FastAPI
uvicorn app.main:app --reload --port 8090
```

**Le serveur démarre sur** : http://localhost:8090

**Documentation** : http://localhost:8090/docs

---

## 🌐 Intégration Frontend (Angular)

### Configuration du Service
```typescript
// src/app/services/ai.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = 'http://localhost:8090';

  constructor(private http: HttpClient) {}

  // 💰 Price Prediction
  predictPrice(propertyData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/price/predict`, propertyData);
  }

  // ⚠️ Risk Scoring
  scoreRisk(tenantData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/scoring/predict`, tenantData);
  }

  // 🏠 Recommendations
  getRecommendations(params: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/recommend/predict`, params);
  }

  // 📈 Market Trend
  getMarketTrends(): Observable<any> {
    return this.http.get(`${this.apiUrl}/trend/trends`);
  }

  getTrendSummary(): Observable<any> {
    return this.http.get(`${this.apiUrl}/trend/summary`);
  }
}
```

### Exemples d'Utilisation

#### 1️⃣ Prédiction de Prix
```typescript
// Dans ton component
export class PropertyFormComponent {
  constructor(private aiService: AiService) {}

  calculateOptimalPrice() {
    const propertyData = {
      surface: this.propertyForm.value.surface,
      rooms: this.propertyForm.value.rooms,
      amenities_count: this.propertyForm.value.amenities.length,
      avg_rating: 4.0, // Valeur par défaut pour nouvelle propriété
      occupancy_rate: 0.7 // Estimation initiale
    };

    this.aiService.predictPrice(propertyData).subscribe({
      next: (response) => {
        console.log('Prix prédit:', response.predicted_price_eth, 'ETH');
        console.log('Prix en EUR:', response.predicted_price_eur, '€');
        console.log('Fourchette:', response.confidence_range_eth);
        
        // Afficher dans le formulaire
        this.suggestedPrice = response.predicted_price_eth;
      },
      error: (error) => console.error('Erreur:', error)
    });
  }
}
```

#### 2️⃣ Évaluation du Risque
```typescript
// Dans la page de réservation
export class BookingComponent {
  constructor(private aiService: AiService) {}

  evaluateTenant(tenantId: number) {
    // Récupérer les données du tenant depuis ton backend
    this.tenantService.getTenant(tenantId).subscribe(tenant => {
      const riskData = {
        income: tenant.income,
        debt_ratio: tenant.debtRatio,
        total_bookings: tenant.totalBookings,
        cancellations: tenant.cancellations,
        late_cancellations: tenant.lateCancellations,
        avg_rating: tenant.avgRating
      };

      this.aiService.scoreRisk(riskData).subscribe({
        next: (response) => {
          console.log('Risk Score:', response.risk_score);
          console.log('Risk Level:', response.risk_level);
          
          // Afficher un badge de risque
          this.riskLevel = response.risk_level;
          this.riskScore = response.risk_score;
          
          // Décision automatique
          if (response.risk_level === 'HIGH') {
            this.showWarning('Locataire à risque élevé');
          }
        }
      });
    });
  }
}
```

#### 3️⃣ Recommandations de Propriétés
```typescript
// Dans la page d'accueil
export class HomeComponent implements OnInit {
  recommendations: any[] = [];

  constructor(private aiService: AiService) {}

  ngOnInit() {
    // Recommandations pour l'utilisateur connecté
    const currentUserId = this.authService.getCurrentUserId();
    
    this.aiService.getRecommendations({
      tenant_id: currentUserId,
      property_id: null,
      top_n: 10
    }).subscribe({
      next: (response) => {
        this.recommendations = response.recommendations;
        console.log('Recommandations:', this.recommendations);
      }
    });
  }

  // Propriétés similaires (sur la page de détails)
  loadSimilarProperties(propertyId: number) {
    this.aiService.getRecommendations({
      tenant_id: null,
      property_id: propertyId,
      top_n: 5
    }).subscribe({
      next: (response) => {
        this.similarProperties = response.recommendations;
      }
    });
  }
}
```

#### 4️⃣ Dashboard des Tendances
```typescript
// Dans le dashboard admin
export class MarketDashboardComponent implements OnInit {
  marketSummary: any;
  neighborhoods: any[] = [];

  constructor(private aiService: AiService) {}

  ngOnInit() {
    // Résumé du marché
    this.aiService.getTrendSummary().subscribe({
      next: (summary) => {
        this.marketSummary = summary;
        console.log('Prix moyen:', summary.avg_price_eth, 'ETH');
      }
    });

    // Toutes les tendances
    this.aiService.getMarketTrends().subscribe({
      next: (response) => {
        this.neighborhoods = response.trends;
        
        // Afficher sur une carte ou un graphe
        this.renderHeatmap(this.neighborhoods);
      }
    });
  }
}
```

### Gestion des Erreurs
```typescript
// Interceptor pour gérer les erreurs
@Injectable()
export class AiErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 503) {
          // Service indisponible
          console.error('AI Service non disponible');
        } else if (error.status === 422) {
          // Erreur de validation
          console.error('Données invalides:', error.error.detail);
        }
        return throwError(() => error);
      })
    );
  }
}
```

---

## 📊 Datasets

Les datasets sont générés automatiquement et stockés dans `datasets/raw/` :

| Dataset | Lignes | Colonnes | Description |
|---------|--------|----------|-------------|
| **tenant_risk.csv** | 1000 | 7 | Données locataires (income, debt_ratio, cancellations, etc.) |
| **property_price.csv** | 300 | 8 | Propriétés (surface, rooms, amenities, prix ETH/EUR) |
| **recommendation.csv** | 5000 | 3 | Ratings tenant-propriété (tenant_id, property_id, rating) |

### Régénérer les Datasets
```bash
python datasets/generate_datasets.py
```

**Note** : La régénération écrase les anciens fichiers. 

---

## ⚡ Performance

| Endpoint | Temps de Réponse | Métriques |
|----------|------------------|-----------|
| `/price/predict` | < 30ms | MAE: 0.0106 ETH, R²: 97.4% |
| `/scoring/predict` | < 25ms | MAE: 2.47 pts, R²: 98.5% |
| `/recommend/predict` | < 50ms | Similarité cosine |
| `/trend/trends` | < 40ms | 2 clusters optimaux |

**Capacité** :
- ✅ 100+ requêtes/seconde par endpoint
- ✅ Modèles chargés en mémoire (pas de rechargement)
- ✅ Temps de démarrage : ~2 secondes

---

## 📚 Documentation

### Swagger UI
- **URL** : http://localhost:8090/docs
- **Fonctionnalités** :
    - ✅ Tester tous les endpoints
    - ✅ Voir les schémas de données
    - ✅ Exemples de requêtes/réponses
    - ✅ Codes d'erreur

### Notebook d'Analyse
- **Fichier** : `notebooks/ai_service_analysis.ipynb`
- **Contenu** :
    - 📊 Visualisations interactives (Plotly)
    - 📈 Analyses de corrélations
    - 🔍 Distribution des données
    - 🎯 Feature importance

**Lancer Jupyter** :
```bash
cd notebooks
jupyter notebook
# Ouvrir ai_service_analysis.ipynb
```

---

## 🔒 CORS & Sécurité

### CORS Configuré

Le service accepte les requêtes depuis :
- ✅ `http://localhost:4200` (Angular dev)
- ✅ `http://localhost:8080` (Backend Java)

**Configuration** : `app/main.py`

### Headers Acceptés
- `Content-Type`
- `Authorization`
- `Accept`

### Méthodes
- `GET`, `POST`, `PUT`, `DELETE`

---

## 🧪 Tests

### Tester les Endpoints

**Via Swagger** : http://localhost:8090/docs

**Via curl** :
```bash
# Price Prediction
curl -X POST http://localhost:8090/price/predict \
  -H "Content-Type: application/json" \
  -d '{
    "surface": 85,
    "rooms": 3,
    "amenities_count": 8,
    "avg_rating": 4.4,
    "occupancy_rate": 0.72
  }'

# Risk Scoring
curl -X POST http://localhost:8090/scoring/predict \
  -H "Content-Type: application/json" \
  -d '{
    "income": 7000,
    "debt_ratio": 0.3,
    "total_bookings": 5,
    "cancellations": 1,
    "late_cancellations": 0,
    "avg_rating": 4.5
  }'

# Recommendations
curl -X POST http://localhost:8090/recommend/predict \
  -H "Content-Type: application/json" \
  -d '{
    "tenant_id": 1,
    "property_id": null,
    "top_n": 5
  }'

# Market Trends
curl http://localhost:8090/trend/summary
```

### Health Checks
```bash
# Global health
curl http://localhost:8090/health

# Price model
curl http://localhost:8090/price/health

# Risk model
curl http://localhost:8090/scoring/health

# Recommendation model
curl http://localhost:8090/recommend/health

# Trend model
curl http://localhost:8090/trend/health
```

---

## 📦 Structure du Projet
```
ai-service/
├── app/
│   ├── main.py                 # Point d'entrée FastAPI
│   ├── routers/
│   │   ├── price.py            # Endpoints Price Prediction
│   │   ├── scoring.py          # Endpoints Risk Scoring
│   │   ├── recommend.py        # Endpoints Recommendations
│   │   └── trend.py            # Endpoints Market Trend
│   ├── services/
│   │   ├── price_model.py      # Entraînement modèle Price
│   │   ├── scoring_model.py    # Entraînement modèle Risk
│   │   ├── recommend_model.py  # Entraînement modèle Recommend
│   │   └── trend_model.py      # Entraînement modèle Trend
│   └── schemas/
│       ├── price.py            # Schemas Pydantic Price
│       ├── scoring.py          # Schemas Pydantic Risk
│       ├── recommendation.py   # Schemas Pydantic Recommend
│       └── trend.py            # Schemas Pydantic Trend
├── datasets/
│   ├── generate_datasets.py   # Génération datasets
│   └── raw/
│       ├── tenant_risk.csv
│       ├── property_price.csv
│       └── recommendation.csv
├── models/
│   ├── price_prediction_model.pkl
│   ├── risk_scoring_model.pkl
│   ├── recommendation_model.pkl
│   └── market_trend_model.pkl
├── notebooks/
│   ├── ai_service_analysis.ipynb
│   
├── requirements.txt
└── README.md
```

---

## 🚨 Troubleshooting

### Le serveur ne démarre pas
```bash
# Vérifier que le port 8090 est libre
netstat -an | findstr 8090

# Tuer le processus si nécessaire
taskkill /F /PID <PID>

# Relancer
uvicorn app.main:app --reload --port 8090
```

### Erreur "Modèle non trouvé"
```bash
# Ré-entraîner les modèles
python app/services/price_model.py
python app/services/scoring_model.py
python app/services/recommend_model.py
python app/services/trend_model.py
```

### Erreur CORS depuis Angular

Vérifier que `http://localhost:4200` est dans la liste CORS (`app/main.py`).

### Prédictions incohérentes

Vérifier les ranges de validation dans les schemas :
- `app/schemas/price.py`
- `app/schemas/scoring.py`
- `app/schemas/recommendation.py`


- ⚠️ Le dossier venv/ n'est pas versionné.
- Chacun doit créer son propre environnement virtuel à partir de requirements.txt.

