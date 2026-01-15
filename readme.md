## Ports

| Service              | Port | Responsabilité |
|---------------------|------|----------------|
| User Service         | 8081 | Inscription, connexion, profil, vérification email/SMS, wallets |
| Listing Service      | 8082 | Gestion annonces, disponibilités, prix, recherche |
| Booking Service      | 8083 | Réservations, calcul prix, gestion statuts |
| Payment Service      | 8084 | Paiements blockchain, escrow, vérification solde ETH |
| Messaging Service    | 8085 | Chat temps réel entre locataire et propriétaire |
| Notification Service | 8086 | Emails et SMS (confirmation, rappels) |
| Review Service       | 8087 | Avis et notes des locataires |
| Media Service        | 8088 | Upload/compression photos, stockage S3 |
| Blockchain Service   | 8089 | Interface avec smart contracts Ethereum |
| AI Service           | 8090 | Recommandations, scoring, détection fraude, assistance intelligente |
| API Gateway          | 8080 | Point d’entrée unique, routage, sécurité, rate limiting |
| Eureka Server        | 8761 | Service discovery des microservices |
| Front Service        | 4200 | Application frontend (Angular / React) |





🧭 Roadmap – Communication entre Microservices

Objectif : documenter l’état d’avancement des communications entre microservices
✅ = fait
⚠️ = optionnel / plus tard
❌ = non implémenté
🟡 = restant à faire

🔗 Vue d’ensemble

Architecture microservices

Booking Service = chef d’orchestre

Communication REST + Events

Messaging Service → sera ajouté plus tard

Review Service → optionnel

🧠 BOOKING SERVICE (Port 8083) – CHEF D’ORCHESTRE
📝 Commentaire global

Done listing service com – remaining messaging service com
👉 Les communications critiques sont terminées
👉 Messaging sera ajouté après

📤 Envoie des messages à :
👤 USER SERVICE (8081) ✅ DONE

Vérifier si l’utilisateur existe

Récupérer l’email (notifications)

Récupérer le wallet (paiements)

✔️ Communication complète et fonctionnelle

🏠 PROPERTY / LISTING SERVICE (8082) ✅ DONE

Vérifier si la propriété existe

Récupérer les prix :

pricePerNight

cleaningFee

petFee

Vérifier la disponibilité

Bloquer les dates (après confirmation)

Débloquer les dates (après annulation)

✔️ Listing service totalement intégré

💳 PAYMENT SERVICE (8084) ✅ DONE

Initier le paiement

Libérer l’escrow après check-out

Initier un remboursement après annulation

✔️ Paiements + escrow opérationnels

📧 NOTIFICATION SERVICE (8086) ✅ DONE

Email de confirmation

Email d’annulation

Rappels check-in / check-out

✔️ Notifications prêtes

📥 Reçoit des messages de :
👤 USER SERVICE (8081) ✅ DONE

Infos utilisateur (email, wallet)

🏠 PROPERTY SERVICE (8082) ✅ DONE

Infos propriété

Disponibilité

Confirmation blocage / déblocage

💳 PAYMENT SERVICE (8084) ✅ DONE

Paiement confirmé (txHash)

Escrow libéré

Remboursement effectué

👤 USER SERVICE (Port 8081)
📝 Commentaire

Service autonome – aucune dépendance sortante

📤 Envoie des messages à :

❌ Aucun

📥 Reçoit des messages de :
🧠 BOOKING SERVICE (8083) ✅ DONE

Requête : Infos utilisateur

Réponse → Booking Service

💬 MESSAGING SERVICE (8085) ⚠️ OPTIONNEL

Requête : Infos utilisateur pour le chat

🟡 À faire plus tard

🏠 PROPERTY SERVICE (Port 8082) – LISTING SERVICE
📝 Commentaire

Done ✅

📤 Envoie des messages à :
⛓ BLOCKCHAIN SERVICE (8089) ⚠️ OPTIONNEL

Enregistrer propriété on-chain

🟡 Peut être ajouté plus tard

📥 Reçoit des messages de :
🧠 BOOKING SERVICE (8083) ✅ DONE

Infos propriété

Vérifier disponibilité

Bloquer dates

Débloquer dates

💳 PAYMENT SERVICE (Port 8084)
📝 Commentaire

Done ✅

📤 Envoie des messages à :
⛓ BLOCKCHAIN SERVICE (8089) ✅ DONE

Exécuter transaction blockchain

Libérer escrow via smart contract

🧠 BOOKING SERVICE (8083) ✅ DONE

Événement : Paiement confirmé

📧 NOTIFICATION SERVICE (8086) ✅ DONE

Commande : Envoyer notification paiement

📥 Reçoit des messages de :
🧠 BOOKING SERVICE (8083) ✅ DONE

Initier paiement

Libérer escrow

Rembourser

⛓ BLOCKCHAIN SERVICE (8089) ✅ DONE

Transaction confirmée on-chain

💬 MESSAGING SERVICE (Port 8085)
📝 Commentaire

⚠️ Pas encore implémenté
👉 Sera ajouté après les communications critiques

📤 Envoie des messages à :

👤 User Service (8081) ⚠️

📥 Reçoit des messages de :

🧠 Booking Service (8083) ⚠️

Nouvelle réservation → créer conversation

📧 NOTIFICATION SERVICE (Port 8086)
📝 Commentaire

Done ✅ – service terminal

📤 Envoie des messages à :

❌ Aucun

📥 Reçoit des messages de :

🧠 Booking Service (8083) ✅

💳 Payment Service (8084) ✅

⭐ Review Service (8087) ⚠️

⭐ REVIEW SERVICE (Port 8087)
📝 Commentaire

⚠️ Optionnel – maybe later

📤 Envoie des messages à :

📧 Notification Service (8086) ⚠️

📥 Reçoit des messages de :

🧠 Booking Service (8083) ⚠️

🖼 MEDIA SERVICE (Port 8088)
📝 Commentaire

⚠️ Optionnel

📤 Envoie des messages à :

❌ Aucun

📥 Reçoit des messages de :

🏠 Property Service (8082) ⚠️

⛓ BLOCKCHAIN SERVICE (Port 8089)
📝 Commentaire

Done for payments – property on-chain optional

📤 Envoie des messages à :

💳 Payment Service (8084) ✅

📥 Reçoit des messages de :

💳 Payment Service (8084) ✅

🏠 Property Service (8082) ⚠️

✅ RÉSUMÉ RAPIDE
Service	Statut
Booking	✅ Done
User	✅ Done
Property	✅ Done
Payment	✅ Done
Notification	✅ Done
Blockchain	✅ Done
Messaging	⚠️ Later
Review	⚠️ Optional
Media	⚠️ Optional
---

## 📊 Schéma Global des Communications

```text
┌─────────────────────────────────────────────────────────────┐
│                    BOOKING SERVICE (8083)                    │
│                   ★ CHEF D'ORCHESTRE ★                      │
└─────────────────────────────────────────────────────────────┘
        │ │ │ │
        │ │ │ └──────────────────┐
        │ │ │                    ▼
        │ │ │         ┌──────────────────────┐
        │ │ │         │ NOTIFICATION (8086)  │
        │ │ │         │ - Emails / SMS       │
        │ │ │         └──────────────────────┘
        │ │ │
        │ │ └─────────────────┐
        │ │                   ▼
        │ │         ┌──────────────────────┐
        │ │         │   PAYMENT (8084)     │◄────┐
        │ │         └──────────────────────┘     │
        │ │                   │                  │
        │ │                   ▼                  │
        │ │         ┌──────────────────────┐     │
        │ │         │  BLOCKCHAIN (8089)   │─────┘
        │ │         └──────────────────────┘
        │ │
        │ └──────────────┐
        │                ▼
        │      ┌──────────────────────┐
        │      │   PROPERTY (8082)    │
        │      └──────────────────────┘
        │
        └─────────────┐
                      ▼
            ┌──────────────────────┐
            │    USER (8081)       │
            └──────────────────────┘
```

---

## ✅ Communications Critiques (Obligatoires)

* Booking ↔ User
* Booking ↔ Property
* Booking → Payment
* Payment ↔ Blockchain
* Booking → Notification

---

## ⚠️ Communications Optionnelles

* Booking → Messaging
* Booking → Review
* Property → Media
* Property → Blockchain

---


