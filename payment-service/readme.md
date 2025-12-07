
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

