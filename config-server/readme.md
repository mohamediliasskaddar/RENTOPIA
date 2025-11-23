# Config Server - Rental Platform (Spring Boot 3.5.8)

## 📋 Description  -  to know more about this if  someone wants to test it 
Serveur de configuration centralisé pour la plateforme de location décentralisée. Gère toutes les configurations des microservices via un dépôt Git local.

NOTE : while creating this microservice i didnt find the same  version i was working with  so i worked with this one (it won't cause a problem )
**Version Spring Boot** : 3.5.8   
**Version Spring Cloud** : 2023.0.0

---

## 🚀 Nouveautés Spring Boot 3.5.8



---

## 📁 Structure du projet
```
config-server/
├── config-repo/                          # Dépôt Git des configurations
│   ├── .git/                            # Dépôt Git initialisé
│   ├── application.yaml                 # Config globale (tous les services)
│   ├── application-dev.yaml             # Config globale DEV
│   ├── application-prod.yaml            # Config globale PROD
│   ├── api-gateway.yaml                 # Config API Gateway
│   ├── user-service.yaml                # Config User Service
│   ├── listing-service.yaml             # Config Listing Service
│   ├── booking-service.yaml             # Config Booking Service
│   ├── payment-service.yaml             # Config Payment Service
│   ├── messaging-service.yaml           # Config Messaging Service
│   ├── notification-service.yaml        # Config Notification Service
│   ├── review-service.yaml              # Config Review Service
│   ├── media-service.yaml               # Config Media Service
│   └── blockchain-service.yaml          # Config Blockchain Service
│
├── src/main/java/com/rental/configserver/
│   ├── config/
│   │   └── SecurityConfig.java          # Configuration sécurité
│   ├── controller/
│   │   └── HealthController.java        # Endpoints santé
│   └── ConfigServerApplication.java     # Classe principale
│
├── src/main/resources/
│   └── application.yaml                 # Config du Config Server
│
├── pom.xml                              # Dépendances Maven
└── README.md                            # Ce fichier
```

---

## 🔧 Prérequis

- **Java 17+** (obligatoire pour Spring Boot 3.x)
- **Maven 3.8+**
- **Git** (pour le versioning des configs)
- **Eureka Server** en cours d'exécution (port 8761)

**Vérifier les versions** :
```bash
java -version    # Doit afficher 17 ou supérieur
mvn -version     # Doit afficher 3.8 ou supérieur
git --version
```

---

## 📦 Installation

### Étape 1 : Cloner/Créer le projet

Si vous avez déjà le projet, passez à l'étape 2.

### Étape 2 : Initialiser le dépôt Git des configurations
```bash
cd config-server
cd config-repo

# Initialiser Git
git init

# Ajouter tous les fichiers .yaml
git add *.yaml

# Premier commit
git commit -m "Initial configuration files"

# Vérifier
git log --oneline
```

⚠️ **IMPORTANT** : Sans Git, Config Server ne démarrera pas !

### Étape 3 : Installer les dépendances
```bash
cd ..  # Retour dans config-server/
mvn clean install
```

---

## 🚀 Démarrage

### Mode par défaut
```bash
mvn spring-boot:run
```

### Mode développement
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Mode production
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**Logs de démarrage attendus** :
```
   ____             __ _        ____                            
  / ___|___  _ __  / _(_) __ _ / ___|  ___ _ ____   _____ _ __ 
 | |   / _ \| '_ \| |_| |/ _` | \___ \ / _ \ '__\ \ / / _ \ '__|
 | |__| (_) | | | |  _| | (_| |  ___) |  __/ |   \ V /  __/ |   
  \____\___/|_| |_|_| |_|\__, | |____/ \___|_|    \_/ \___|_|   
                         |___/                                   

Started ConfigServerApplication in 5.234 seconds
Tomcat started on port 8888
```

---

## 🌐 Endpoints

### 1. Récupérer les configurations

**Format** : `http://localhost:8888/{service-name}/{profile}`

#### Configuration globale
```bash
# Configuration par défaut (tous les services)
GET http://localhost:8888/application/default

# Configuration développement
GET http://localhost:8888/application/dev

# Configuration production
GET http://localhost:8888/application/prod
```

#### Configuration spécifique à un service
```bash
# User Service - profil par défaut
GET http://localhost:8888/user-service/default

# User Service - profil dev
GET http://localhost:8888/user-service/dev

# API Gateway - profil par défaut
GET http://localhost:8888/api-gateway/default

# Booking Service - profil par défaut
GET http://localhost:8888/booking-service/default
```

### 2. Endpoints de santé
```bash
# Health check simple
GET http://localhost:8888/api/config/health

# Informations sur le serveur
GET http://localhost:8888/api/config/info
```

### 3. Endpoints Actuator
```bash
# Health check détaillé
GET http://localhost:8888/actuator/health

# Liste des endpoints disponibles
GET http://localhost:8888/actuator

# Variables d'environnement
GET http://localhost:8888/actuator/env

# Propriétés de configuration
GET http://localhost:8888/actuator/configprops
```

---

## 🔐 Sécurité

### Credentials par défaut

- **Username** : `configuser`
- **Password** : `configpass123`

### Authentification

Toutes les requêtes nécessitent une authentification HTTP Basic :
```bash
# Avec curl
curl -u configuser:configpass123 http://localhost:8888/user-service/default

# Ou directement dans l'URL
curl http://configuser:configpass123@localhost:8888/user-service/default
```


```


---

