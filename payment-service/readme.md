
## 🎯  Démarrage de l'Application**

### Ordre de démarrage :
```bash
# 1. MySQL (doit être déjà démarré)

# 2. RabbitMQ
# Télécharger: https://www.rabbitmq.com/download.html
# Démarrer: rabbitmq-server

# 3. Eureka Server (Port 8761)
cd eureka-server
mvn spring-boot:run


# 5. API Gateway (Port 8080)
cd api-gateway
mvn spring-boot:run

# 6. Payment Service (Port 8084)
cd payment-service
mvn clean install
mvn spring-boot:run
```

---

## 🧪 **ÉTAPE 12 : Tests Postman**



### 12.1 Health Check
```http
GET http://localhost:8084/api/payments/health
```

**Réponse attendue** :
Payment Service is running! 🚀

---

### 12.2 Vérifier un Solde
```http
GET http://localhost:8084/api/payments/balance/0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb
```

**Réponse** :
```json
{
  "walletAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "balanceEth": 0.5,
  "balanceUsd": null,
  "message": "Solde récupéré avec succès"
}
```

---

### 12.3 Créer un Paiement
```http
POST http://localhost:8084/api/payments/create
Content-Type: application/json

{
  "reservationId": 1,
  "hostId": 2,
  "tenantId": 1,
  "tenantWalletAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "hostWalletAddress": "0xYOUR_HOST_WALLET_ADDRESS",
  "amountEth": 0.01,
  "totalAmountEth": 0.012
}
```

**Réponse** :
```json
{
  "transactionId": 1,
  "transactionHash": "0xabc123...",
  "status": "PROCESSING",
  "amountEth": 0.01,
  "gasFeeEth": null,
  "blockNumber": null,
  "createdAt": "2025-12-06T10:30:00",
  "message": "Paiement en cours de traitement",
  "explorerUrl": "https://sepolia.etherscan.io/tx/0xabc123..."
}
```

---

### 12.4 Confirmer une Transaction
```http
GET http://localhost:8084/api/payments/confirm/0xVOTRE_TX_HASH
```

**Réponse** :
```json
{
  "transactionId": 1,
  "transactionHash": "0xabc123...",
  "status": "CONFIRMED",
  "amountEth": 0.01,
  "gasFeeEth": 0.00021,
  "blockNumber": 4567890,
  "createdAt": "2025-12-06T10:30:00",
  "message": "Paiement confirmé avec succès",
  "explorerUrl": "https://sepolia.etherscan.io/tx/0xabc123..."
}
```

---

### 12.5 Historique Réservation
```http
GET http://localhost:8084/api/payments/reservation/1
```

**Réponse** :
```json
[
  {
    "transactionId": 1,
    "transactionHash": "0xabc123...",
    "status": "CONFIRMED",
    "amountEth": 0.01,
    "gasFeeEth": 0.00021,
    "blockNumber": 4567890,
    "createdAt": "2025-12-06T10:30:00",
    "explorerUrl": "https://sepolia.etherscan.io/tx/0xabc123..."
  }
]
```

---





1. Communication avec le Service Blockchain
   Problème initial : Les appels Feign au service blockchain utilisaient des ResponseEntity dans les interfaces, ce qui causait des incompatibilités.

Solution : Simplification des interfaces Feign pour retourner directement les DTO (Map<String, Object>) au lieu de ResponseEntity.

Impact : Communication plus fiable et code plus propre.

2. Gestion des Paiements Signés (Web3/MetaMask)
   Nouvelle fonctionnalité : Implémentation d'un endpoint /confirm-payment pour recevoir et traiter les transactions signées par MetaMask.

Processus :

Réception du hash de transaction signé

Enregistrement en base de données

Récupération de l'adresse du propriétaire via booking-service

Mise à jour du statut de la réservation

Envoi de notification

Publication d'événement RabbitMQ

3. Libération d'Escrow Corrigée
   Problème initial : Appel à une méthode release-funds qui n'existait pas dans le contrat.

Solution : Utilisation de la méthode checkout() du contrat intelligent pour libérer les fonds.

Sécurité : Utilisation de la clé privée admin configurée dans application.yml au lieu d'une valeur hardcodée.

4. Health Check Amélioré
   Fonctionnalité : Endpoint /health complet vérifiant toutes les dépendances :

Base de données

Service blockchain

RabbitMQ

Booking service

Retour détaillé : Statut individuel de chaque service avec informations diagnostiques.

5. Gestion des Erreurs et Résilience
   Fallback Patterns : Implémentation de clients Feign avec fallback pour tous les services externes.

Retry Automatique : Configuration de retry avec backoff exponentiel.

Circuit Breaker : Configuration Resilience4j pour éviter les cascades d'échecs.

🔗 Points d'Intégration Clés
Avec le Service Blockchain
Création de réservation : POST /api/blockchain/bookings/create

Check-in/Check-out : Endpoints pour mettre à jour l'état des réservations

Libération de fonds : Via checkout() du contrat

Création de wallets : Pour les nouveaux utilisateurs

Avec le Service Booking
Récupération des détails : Pour obtenir les adresses wallet des parties

Confirmation de paiement : Mise à jour du statut des réservations

Libération d'escrow : Synchronisation avec le système de réservation

Avec le Service Notification
Notifications de paiement : PAYMENT_RECEIVED, PAYMENT_FAILED

Notifications de réservation : BOOKING_CONFIRMATION, etc.

Récupération d'historique : Pour afficher les notifications liées aux paiements

🚀 Flux de Paiement Typique
Initiation : L'utilisateur sélectionne une propriété et initie un paiement

Signature : L'utilisateur signe la transaction avec MetaMask

Confirmation : Le frontend envoie le hash signé au endpoint /confirm-payment

Traitement :

Enregistrement en base

Vérification du solde

Mise à jour de la réservation

Notification aux parties

Publication d'événement

Libération : Après le check-out, l'escrow est libéré via checkout()


Test #2 : Health Check Inter-Services
bash
# Vérification complète des dépendances
curl "http://localhost:8084/api/payments/health"


fichier ajoutee au blockchain :
dossier dto: SetPropertyOwnerRequest
et modification au niveau du controller : ce qui concerne recuperation du temps + Enregistrer un propriétaire pour un propertyId
