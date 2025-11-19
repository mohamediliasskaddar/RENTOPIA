# Eureka Server - Rental Platform

## Description
Service Discovery Server pour la plateforme de location décentralisée. Gère l'enregistrement et la découverte automatique de tous les microservices.

## Port
- **8761** (port par défaut Eureka)

## Dashboard
Une fois démarré, accédez au dashboard : **http://localhost:8761**

## Prérequis
- Java 17+
- Maven 3.6+

## Démarrage

### Mode développement
```bash
mvn clean install
mvn spring-boot:run
```

Ou avec profil dev :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Mode production
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Endpoints

### Dashboard Web
- **http://localhost:8761** - Interface web Eureka

### API Endpoints
- **GET** `/api/eureka/health` - Health check
- **GET** `/api/eureka/info` - Informations sur le serveur

### Actuator
- **GET** `/actuator/health` - Health check détaillé
- **GET** `/actuator/info` - Informations applicatives
- **GET** `/actuator/metrics` - Métriques

## Sécurité

### Credentials par défaut
- **Username** : admin
- **Password** : admin123

⚠️ **IMPORTANT** : Changez ces credentials en production !

## Services enregistrés

Les services suivants devraient s'enregistrer automatiquement :

1. **api-gateway** (Port 8080)
2. **user-service** (Port 8081)
3. **listing-service** (Port 8082)
4. **booking-service** (Port 8083)
5. **payment-service** (Port 8084)
6. **messaging-service** (Port 8085)
7. **notification-service** (Port 8086)
8. **review-service** (Port 8087)
9. **media-service** (Port 8088)
10. **blockchain-service** (Port 8089)

## Configuration

### Self-Preservation Mode
- **Développement** : Désactivé (pour éviter les instances fantômes)
- **Production** : Activé (pour gérer les problèmes réseau temporaires)

### Eviction Interval
- **Développement** : 5 secondes
- **Production** : 30 secondes

## Enregistrement d'un service client

Pour enregistrer un microservice dans Eureka, ajoutez dans son `application.yml` :
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://admin:admin123@localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

## Troubleshooting

### Le serveur ne démarre pas
1. Vérifier que le port 8761 est libre
2. Vérifier les logs : `mvn spring-boot:run`
3. Vérifier la version Java : `java -version`

### Les services ne s'enregistrent pas
1. Vérifier que Eureka Server est en cours d'exécution
2. Vérifier l'URL dans la configuration client
3. Vérifier les credentials si la sécurité est activée
4. Consulter les logs du microservice

### Dashboard vide
1. Attendre 30 secondes après le démarrage d'un service
2. Vérifier que les services ont bien `register-with-eureka: true`
3. Vérifier les logs d'Eureka

## Monitoring

### Vérifier les services enregistrés
```bash
curl http://admin:admin123@localhost:8761/eureka/apps
```

### Vérifier le statut
```bash
curl http://localhost:8761/actuator/health
```

## Architecture
```
┌─────────────────────┐
│   Eureka Server     │
│     (Port 8761)     │
└──────────┬──────────┘
           │
           │ Registration & Discovery
           │
    ┌──────┴───────┬───────────┬──────────┐
    │              │           │          │
┌───▼────┐   ┌────▼─────┐  ┌──▼─────┐  ...
│  API   │   │  User    │  │ Listing│
│ Gateway│   │ Service  │  │ Service│
└────────┘   └──────────┘  └────────┘
```

## Auteur
Rental Platform Team
```

---

## 📋 **ÉTAPE 10 : Structure finale du projet**
```
eureka-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── rental/
│   │   │           └── eurekaserver/
│   │   │               ├── config/
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controller/
│   │   │               │   └── HealthController.java
│   │   │               └── EurekaServerApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
│           └── com/
│               └── rental/
│                   └── eurekaserver/
│                       └── EurekaServerApplicationTests.java
├── pom.xml
└── README.md