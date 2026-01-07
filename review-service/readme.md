## 12. DÉMARRAGE DE L'APPLICATION

### **Étape 1 : Prérequis**

Vérifiez que ces services sont démarrés :

1. ✅ **MySQL** (port 3306)
2. ✅ **Eureka Server** (port 8761)
3. ✅ **RabbitMQ** (port 5672)
4. ✅ **Config Server** (port 8888) - optionnel

### **Étape 2 : Compiler le projet**
```bash
mvn clean install
```

### **Étape 3 : Lancer l'application**

**Option 1 : Via Maven**
```bash
mvn spring-boot:run
```

**Option 2 : Via IDE**
- Clic droit sur `ReviewServiceApplication.java`
- Run

### **Étape 4 : Vérifier le démarrage**

Vous devriez voir dans les logs :
✅ Review Service démarré sur le port 8087

Vérifiez Eureka : http://localhost:8761
- Le service `REVIEW-SERVICE` doit apparaître

---

## 13. TESTS POSTMAN
## COLLECTION POSTMAN COMPLÈTE
```json
{
  "info": {
    "name": "Review Service API Tests",
    "description": "Collection complète de tests pour le Review Service",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Health Check",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/reviews/health"
      }
    },
    {
      "name": "2. Create Review - Excellent",
      "request": {
        "method": "POST",
        "url": "http://localhost:8087/api/reviews",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"reservationId\": 1,\n    \"userId\": 1,\n    \"propertyId\": 1,\n    \"reviewText\": \"Excellent appartement! Très propre et bien situé. Le propriétaire était très accueillant. Je recommande vivement!\",\n    \"ratingValue\": 4.5\n}"
        }
      }
    },
    {
      "name": "3. Create Review - Good",
      "request": {
        "method": "POST",
        "url": "http://localhost:8087/api/reviews",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"reservationId\": 2,\n    \"userId\": 2,\n    \"propertyId\": 1,\n    \"reviewText\": \"Bon séjour, quelques petits détails à améliorer mais dans l'ensemble très satisfaisant.\",\n    \"ratingValue\": 3.5\n}"
        }
      }
    },
    {
      "name": "4. Get Review by ID",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/reviews/1"
      }
    },
    {
      "name": "5. Get Review by Reservation",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/reviews/reservation/1"
      }
    },
    {
      "name": "6. Get Property Reviews",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/reviews/api/property/1"
      }
    },
    {
      "name": "7. Get User Reviews",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/reviews/user/1"
      }
    },
    {
      "name": "8. Update Review",
      "request": {
        "method": "PUT",
        "url": "http://localhost:8087/api/reviews/1?userId=1",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"reviewText\": \"Appartement parfait! Mise à jour de mon avis après quelques jours de réflexion. C'était vraiment excellent!\",\n    \"isVisible\": true\n}"
        }
      }
    },
    {
      "name": "9. Get Property Stats",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/reviews/property/1/stats"
      }
    },
    {
      "name": "10. Get Average Rating",
      "request": {
        "method": "GET",
        "url": "http://localhost:8087/api/reviews/property/1/average-rating"
      }
    },