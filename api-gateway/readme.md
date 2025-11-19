# API Gateway - Rental Platform

## Description
API Gateway pour la plateforme de location décentralisée. Gère le routage, l'authentification JWT, le rate limiting et la protection circuit breaker.

## Port
- **8080**

## Prérequis
- Java 17+
- Maven 3.6+
- Redis Server (pour rate limiting)
- Eureka Server en cours d'exécution (port 8761)

## Démarrage

### 1. Démarrer Redis
```bash
redis-server
```

### 2. Démarrer Eureka Server
Assurez-vous que Eureka Server tourne sur le port 8761

### 3. Démarrer l'API Gateway
```bash
mvn clean install
mvn spring-boot:run
```

Ou avec un profil spécifique :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Endpoints

### Health Check
- **GET** `/api/gateway/health` - Statut de l'API Gateway
- **GET** `/api/gateway/services` - Liste des services enregistrés
- **GET** `/api/gateway/info` - Informations sur la gateway

### Actuator
- **GET** `/actuator/health` - Health check détaillé
- **GET** `/actuator/gateway/routes` - Liste des routes configurées

## Routes

| Service | Path | Authentification |
|---------|------|------------------|
| User Service | `/api/users/**` | Mixte |
| Listing Service | `/api/listings/**` | Mixte |
| Booking Service | `/api/bookings/**` | Requise |
| Payment Service | `/api/payments/**` | Requise |
| Messaging Service | `/api/messages/**` | Requise |
| Notification Service | `/api/notifications/**` | Requise |
| Review Service | `/api/reviews/**` | Mixte |
| Media Service | `/api/media/**` | Requise |
| Blockchain Service | `/api/blockchain/**` | Requise |

## Fonctionnalités

### 1. Authentification JWT
- Validation des tokens JWT
- Extraction des informations utilisateur
- Propagation des headers aux microservices

### 2. Rate Limiting
- Limite basée sur l'IP
- Limite basée sur l'ID utilisateur
- Protection anti-spam

### 3. Circuit Breaker
- Protection contre les pannes en cascade
- Fallback automatique
- Configuration Resilience4J

### 4. CORS
- Configuration multi-origins
- Support des credentials
- Headers personnalisés

### 5. Load Balancing
- Répartition de charge automatique
- Découverte de services via Eureka

## Configuration

### JWT
```yaml
jwt:
  secret: votre-secret-key
  expiration: 86400000
  header: Authorization
  prefix: "Bearer "
```

### Rate Limiting
- **Taux de recharge** : 10 requêtes/seconde
- **Capacité burst** : 20 requêtes
- **Basé sur** : IP ou User ID

### Circuit Breaker
- **Fenêtre** : 10 requêtes
- **Seuil d'échec** : 50%
- **Timeout** : 5 secondes
- **Délai ouverture** : 10 secondes

## Troubleshooting

### L'API Gateway ne démarre pas
1. Vérifier que Redis est en cours d'exécution
2. Vérifier que Eureka Server est accessible
3. Vérifier les logs : `mvn spring-boot:run`

### Les routes ne fonctionnent pas
1. Vérifier que les microservices sont enregistrés dans Eureka
2. Consulter `/actuator/gateway/routes`
3. Vérifier les logs de routing

### Erreurs JWT
1. Vérifier que le secret JWT est identique dans tous les services
2. Vérifier le format du token : `Bearer <token>`
3. Vérifier la date d'expiration du token

## Logs
Les logs sont configurés pour afficher :
- Requêtes entrantes/sortantes
- Validation JWT
- Erreurs de routing
- Circuit breaker events
