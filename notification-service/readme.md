# 📬 Notification Service - Guide Complet

## 📋 Table des matières
- [Vue d'ensemble](#vue-densemble)
- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Démarrage](#démarrage)
- [API Documentation](#api-documentation)
- [Tests Postman](#tests-postman)
- [RabbitMQ Integration](#rabbitmq-integration)
- [Email & SMS](#email--sms)
- [Résolution des problèmes](#résolution-des-problèmes)

---

## 🎯 Vue d'ensemble

Le **Notification Service** est un microservice Spring Boot responsable de la gestion et de l'envoi des notifications dans la plateforme de location décentralisée. Il supporte les notifications in-app, emails et SMS.

**Port:** `8086`

**Technologies:**
- Spring Boot 3.5.8
- Spring Data JPA
- MySQL 8.0
- RabbitMQ (Message Queue)
- Spring Mail (Email)
- Twilio SDK (SMS)
- Thymeleaf (Templates HTML)

---

## ✨ Fonctionnalités

- ✅ **Notifications in-app** : Stockage en base de données des notifications utilisateur
- ✅ **Email** : Envoi d'emails HTML personnalisés via Gmail SMTP
- ✅ **SMS** : Envoi de SMS via Twilio
- ✅ **RabbitMQ** : Communication asynchrone avec les autres microservices
- ✅ **Templates** : Support des templates Thymeleaf pour emails HTML
- ✅ **Gestion du statut** : Marquer comme lu/non-lu
- ✅ **Historique** : Conservation de l'historique des notifications
- ✅ **API RESTful** : Endpoints CRUD complets

### Types de notifications supportés
- `BOOKING_CONFIRMATION` - Confirmation de réservation
- `BOOKING_REMINDER` - Rappel de réservation
- `BOOKING_CANCELLED` - Annulation de réservation
- `PAYMENT_RECEIVED` - Paiement reçu
- `PAYMENT_FAILED` - Échec de paiement
- `CHECK_IN_REMINDER` - Rappel check-in
- `CHECK_OUT_REMINDER` - Rappel check-out
- `REVIEW_REQUEST` - Demande d'avis
- `MESSAGE_RECEIVED` - Message reçu
- `BOOKING_REQUEST_RECEIVED` - Demande de réservation reçue
- `BOOKING_REQUEST_ACCEPTED` - Demande acceptée
- `BOOKING_REQUEST_DECLINED` - Demande refusée

---

## 🏗️ Architecture

```
notification-service/
├── src/main/java/com/rental/notification/
│   ├── NotificationServiceApplication.java
│   ├── config/
│   │   ├── RabbitMQConfig.java         # Configuration RabbitMQ
│   │   ├── EmailConfig.java            # Configuration Email
│   │   └── TwilioConfig.java           # Configuration SMS
│   ├── controller/
│   │   └── NotificationController.java  # API REST
│   ├── dto/
│   │   ├── NotificationRequest.java
│   │   ├── NotificationResponse.java
│   │   ├── EmailRequest.java
│   │   └── SmsRequest.java
│   ├── entity/
│   │   └── Notification.java            # Entité JPA
│   ├── enums/
│   │   └── NotificationType.java
│   ├── exception/
│   │   ├── NotificationException.java
│   │   └── GlobalExceptionHandler.java
│   ├── listener/
│   │   └── BookingEventListener.java    # Listener RabbitMQ
│   ├── repository/
│   │   └── NotificationRepository.java
│   └── service/
│       ├── NotificationService.java
│       ├── EmailService.java
│       └── SmsService.java
├── src/main/resources/
│   ├── application.yml
│   └── templates/
│       └── booking-confirmation.html    # Template email
└── pom.xml
```

---

## 📦 Prérequis

Avant de démarrer, assurez-vous d'avoir installé :

### Obligatoire
- ☕ **Java 17** ou supérieur
- 📦 **Maven 3.6+**
- 🗄️ **MySQL 8.0** (port 3306)
- 🐰 **RabbitMQ 3.13+** (port 5672)
- 🔍 **Eureka Server** (port 8761)

### Optionnel (mais recommandé)
- 📧 **Compte Gmail** avec App Password (pour emails)
- 📱 **Compte Twilio** (pour SMS)
- ⚙️ **Config Server** (port 8888)

---

## 🚀 Installation

### 1. Cloner le repository
```bash
git clone <repo>
cd notification-service
```

### 2. Créer la base de données
La base de données `rental_db` doit déjà exister avec la table `notifications`.

**Structure de la table :**
```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    reservation_id INT,
    booking_request_id INT,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    related_entity_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    sent_via_email BOOLEAN DEFAULT FALSE,
    sent_via_sms BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 3. Installer les dépendances Maven
```bash
mvn clean install
```

---



## 🎬 Démarrage

### Étape 1 : Démarrer les services requis

```bash
# 1. Démarrer MySQL
# Windows: via XAMPP ou MySQL Workbench
# Mac: brew services start mysql
# Linux: sudo systemctl start mysql

# 2. Démarrer RabbitMQ
# Windows: net start RabbitMQ
# Mac: brew services start rabbitmq
# Linux: sudo systemctl start rabbitmq-server

# 3. Démarrer Eureka Server
cd eureka-server
mvn spring-boot:run

# 4. (Optionnel) Démarrer Config Server
cd config-server
mvn spring-boot:run
```

### Étape 2 : Lancer le Notification Service

**Option 1 : Via Maven**
```bash
mvn spring-boot:run
```

**Option 2 : Via JAR**
```bash
mvn clean package
java -jar target/notification-service-1.0.0.jar
```

**Option 3 : Via IDE (IntelliJ/Eclipse)**
- Clic droit sur `NotificationServiceApplication.java`
- Run 'NotificationServiceApplication'

### Étape 3 : Vérifier le démarrage

**Console logs :**
```
✅ Notification Service démarré sur le port 8086
```

**Eureka Dashboard :**
- http://localhost:8761
- Vérifiez que `NOTIFICATION-SERVICE` apparaît dans la liste

**RabbitMQ Management :**
- http://localhost:15672 (guest/guest)
- Vérifiez les queues : `notification.queue`, `email.queue`, `sms.queue`

---

## 📡 API Documentation

### Base URL
```
http://localhost:8086/api/notifications
```

### Endpoints

#### 1. Health Check
```http
GET /api/notifications/health
```

**Response:**
```json
{
    "status": "UP",
    "service": "Notification Service",
    "port": "8086"
}
```

---

#### 2. Créer une notification
```http
POST /api/notifications
Content-Type: application/json
```

**Request Body:**
```json
{
    "userId": 1,
    "reservationId": 1,
    "notificationType": "BOOKING_CONFIRMATION",
    "title": "Réservation confirmée",
    "message": "Votre réservation a été confirmée avec succès!",
    "recipientEmail": "user@example.com",
    "recipientPhone": "+212612345678",
    "sendEmail": true,
    "sendSms": false
}
```

**Response (201 Created):**
```json
{
    "id": 1,
    "userId": 1,
    "reservationId": 1,
    "notificationType": "BOOKING_CONFIRMATION",
    "title": "Réservation confirmée",
    "message": "Votre réservation a été confirmée avec succès!",
    "isRead": false,
    "sentViaEmail": true,
    "sentViaSms": false,
    "createdAt": "2025-12-01T15:30:00"
}
```

---

#### 3. Récupérer toutes les notifications d'un utilisateur
```http
GET /api/notifications/user/{userId}
```

**Exemple:**
```http
GET /api/notifications/user/1
```

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "userId": 1,
        "reservationId": 1,
        "notificationType": "BOOKING_CONFIRMATION",
        "title": "Réservation confirmée",
        "message": "Votre réservation a été confirmée avec succès!",
        "isRead": false,
        "sentViaEmail": true,
        "sentViaSms": false,
        "createdAt": "2025-12-01T15:30:00"
    }
]
```

---

#### 4. Récupérer les notifications non lues
```http
GET /api/notifications/user/{userId}/unread
```

---

#### 5. Compter les notifications non lues
```http
GET /api/notifications/user/{userId}/unread-count
```

**Response:**
```json
{
    "unreadCount": 5
}
```

---

#### 6. Marquer une notification comme lue
```http
PUT /api/notifications/{id}/read
```

**Response:**
```json
{
    "message": "Notification marquée comme lue"
}
```

---

#### 7. Marquer toutes les notifications comme lues
```http
PUT /api/notifications/user/{userId}/read-all
```

**Response:**
```json
{
    "message": "Toutes les notifications marquées comme lues"
}
```

---

#### 8. Supprimer une notification
```http
DELETE /api/notifications/{id}
```

**Response:**
```json
{
    "message": "Notification supprimée avec succès"
}
```

---

## 🧪 Tests Postman

### Collection Postman complète

Importez cette collection dans Postman :

```json
{
  "info": {
    "name": "Notification Service API Tests",
    "description": "Collection complète de tests pour le Notification Service",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Health Check",
      "request": {
        "method": "GET",
        "url": "http://localhost:8086/api/notifications/health"
      }
    },
    {
      "name": "2. Create Simple Notification",
      "request": {
        "method": "POST",
        "url": "http://localhost:8086/api/notifications",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"userId\": 1,\n    \"reservationId\": 1,\n    \"notificationType\": \"BOOKING_CONFIRMATION\",\n    \"title\": \"Réservation confirmée\",\n    \"message\": \"Votre réservation pour l'appartement à Paris a été confirmée!\",\n    \"sendEmail\": false,\n    \"sendSms\": false\n}"
        }
      }
    },
    {
      "name": "3. Create Notification with Email",
      "request": {
        "method": "POST",
        "url": "http://localhost:8086/api/notifications",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"userId\": 1,\n    \"reservationId\": 1,\n    \"notificationType\": \"BOOKING_CONFIRMATION\",\n    \"title\": \"Confirmation de réservation\",\n    \"message\": \"Félicitations! Votre réservation est confirmée. Check-in le 15/01/2025.\",\n    \"recipientEmail\": \"test@example.com\",\n    \"sendEmail\": true,\n    \"sendSms\": false\n}"
        }
      }
    },
    {
      "name": "4. Create Payment Notification",
      "request": {
        "method": "POST",
        "url": "http://localhost:8086/api/notifications",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"userId\": 1,\n    \"reservationId\": 1,\n    \"notificationType\": \"PAYMENT_RECEIVED\",\n    \"title\": \"Paiement reçu\",\n    \"message\": \"Nous avons bien reçu votre paiement de 0.5 ETH.\",\n    \"sendEmail\": false,\n    \"sendSms\": false\n}"
        }
      }
    },
    {
      "name": "5. Get User Notifications",
      "request": {
        "method": "GET",
        "url": "http://localhost:8086/api/notifications/user/1"
      }
    },
    {
      "name": "6. Get Unread Notifications",
      "request": {
        "method": "GET",
        "url": "http://localhost:8086/api/notifications/user/1/unread"
      }
    },
    {
      "name": "7. Count Unread Notifications",
      "request": {
        "method": "GET",
        "url": "http://localhost:8086/api/notifications/user/1/unread-count"
      }
    },
    {
      "name": "8. Mark Notification as Read",
      "request": {
        "method": "PUT",
        "url": "http://localhost:8086/api/notifications/1/read"
      }
    },
    {
      "name": "9. Mark All as Read",
      "request": {
        "method": "PUT",
        "url": "http://localhost:8086/api/notifications/user/1/read-all"
      }
    },
    {
      "name": "10. Delete Notification",
      "request": {
        "method": "DELETE",
        "url": "http://localhost:8086/api/notifications/2"
      }
    },
    {
      "name": "11. Test Multiple Notifications",
      "request": {
        "method": "POST",
        "url": "http://localhost:8086/api/notifications",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"userId\": 1,\n    \"notificationType\": \"CHECK_IN_REMINDER\",\n    \"title\": \"Rappel Check-in\",\n    \"message\": \"N'oubliez pas votre check-in demain à 14h!\",\n    \"sendEmail\": false,\n    \"sendSms\": false\n}"
        }
      }
    },
    {
      "name": "12. Booking Cancelled Notification",
      "request": {
        "method": "POST",
        "url": "http://localhost:8086/api/notifications",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"userId\": 2,\n    \"reservationId\": 5,\n    \"notificationType\": \"BOOKING_CANCELLED\",\n    \"title\": \"Réservation annulée\",\n    \"message\": \"Votre réservation #5 a été annulée. Remboursement en cours.\",\n    \"recipientEmail\": \"user2@example.com\",\n    \"sendEmail\": true,\n    \"sendSms\": false\n}"
        }
      }
    }
  ]
}
```

### Scénario de test complet

**Test Flow (dans l'ordre) :**

1. ✅ **Health Check** - Vérifier que le service fonctionne
2. ✅ **Create Simple Notification** - Créer une notification basique
3. ✅ **Get User Notifications** - Récupérer toutes les notifications
4. ✅ **Count Unread** - Compter les non lues (devrait être 1)
5. ✅ **Mark as Read** - Marquer comme lue
6. ✅ **Get Unread** - Vérifier (devrait être vide)
7. ✅ **Create with Email** - Tester l'envoi d'email
8. ✅ **Create Multiple** - Créer plusieurs notifications
9. ✅ **Mark All as Read** - Tout marquer comme lu
10. ✅ **Delete** - Supprimer une notification

---

## 🐰 RabbitMQ Integration

### Queues créées automatiquement

Le service crée automatiquement 3 queues :
- `notification.queue` - Notifications générales
- `email.queue` - File d'attente emails
- `sms.queue` - File d'attente SMS

### Exchange et Routing Keys

- **Exchange:** `notification.exchange` (Topic)
- **Routing Keys:**
  - `notification.routing.key`
  - `email.routing.key`
  - `sms.routing.key`

### Tester l'intégration RabbitMQ

1. **Vérifier dans RabbitMQ Management:**
   ```
   http://localhost:15672
   Login: guest / guest
   ```

2. **Vérifier les queues:**
   - Allez dans l'onglet "Queues"
   - Vous devriez voir les 3 queues créées

3. **Publier un message manuellement:**
   - Cliquez sur `notification.queue`
   - Allez dans "Publish message"
   - Payload:
   ```json
   {
       "userId": 1,
       "type": "BOOKING_CONFIRMATION",
       "title": "Test RabbitMQ",
       "message": "Message depuis RabbitMQ",
       "email": "test@example.com"
   }
   ```
   - Publish message
   - Vérifiez les logs du service

---




---

## 🔧 Résolution des problèmes

### Problème 1 : Service ne démarre pas

**Erreur:** `Port 8086 is already in use`

**Solution:**
```bash
# Windows
netstat -ano | findstr :8086
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8086
kill -9 <PID>
```

---

### Problème 2 : Erreur de connexion MySQL

**Erreur:** `Access denied for user 'root'@'localhost'`

**Solution:**
1. Vérifiez que MySQL est démarré
2. Vérifiez username/password dans `application.yml`
3. Testez la connexion:
   ```bash
   mysql -u root -p
   ```

---

### Problème 3 : RabbitMQ non accessible

**Erreur:** `Connection refused: localhost:5672`

**Solution:**
```bash
# Windows
net start RabbitMQ

# Mac
brew services start rabbitmq

# Linux
sudo systemctl start rabbitmq-server
```

Vérifiez le statut:
```bash
rabbitmqctl status
```

---

### Problème 4 : Email non envoyé

**Erreur:** `AuthenticationFailedException`

**Solutions:**
1. Utilisez un **App Password** (pas le mot de passe Gmail)
2. Activez la validation en 2 étapes
3. Vérifiez que les ports SMTP ne sont pas bloqués
4. Testez avec un autre email

---

### Problème 5 : Eureka non trouvé

**Erreur:** `Connection refused: localhost:8761`

**Solution:**
1. Démarrez Eureka Server d'abord
2. Attendez 30 secondes pour l'enregistrement
3. Vérifiez : http://localhost:8761

---

### Problème 6 : Notification créée mais email non envoyé

**Vérifications:**
1. Vérifiez les logs pour les erreurs
2. Vérifiez la configuration email dans `application.yml`
3. Testez manuellement avec un client email
4. Vérifiez `sentViaEmail` dans la réponse

---

## 📊 Monitoring

### Actuator Endpoints

```bash
# Health check
GET http://localhost:8086/actuator/health

# Métriques
GET http://localhost:8086/actuator/metrics

# Info
GET http://localhost:8086/actuator/info
```

### RabbitMQ Dashboard

```
http://localhost:15672
Username: guest
Password: guest
```

**Métriques disponibles:**
- Nombre de messages dans les queues
- Taux de consommation
- Connexions actives


---

## 📝 Notes importantes


### Performance
- RabbitMQ permet le traitement asynchrone
- Les emails sont envoyés en tâche de fond
- La base de données stocke l'historique complet

---


## ✅ Checklist avant déploiement

- [ ] MySQL configuré et accessible
- [ ] RabbitMQ installé et démarré
- [ ] Eureka Server opérationnel
- [ ] Configuration email testée (Gmail App Password)
- [ ] Configuration SMS testée (Twilio)
- [ ] Tous les tests Postman passent
- [ ] Les queues RabbitMQ sont créées
- [ ] Le service s'enregistre sur Eureka
- [ ] Les logs ne montrent pas d'erreurs
- [ ] Health check retourne UP

---
 

---

🎉 **Le Notification Service est prêt à l'emploi !**