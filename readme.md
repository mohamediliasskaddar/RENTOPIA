## RoadMap

🔗 COMMUNICATION ENTRE MICROSERVICES - VUE D'ENSEMBLE
## ---------> Done listing service com remainig 
BOOKING SERVICE (Port 8083) - LE CHEF D'ORCHESTRE
📤 ENVOIE DES MESSAGES À : 

USER SERVICE (8081) ✅

Vérifier si l'utilisateur existe
Récupérer email pour notifications
Récupérer wallet pour paiements


PROPERTY SERVICE (8082) ✅

Vérifier si la propriété existe
Récupérer les prix (pricePerNight, cleaningFee, petFee)
Vérifier la disponibilité
Bloquer les dates (après confirmation)
Débloquer les dates (après annulation)


PAYMENT SERVICE (8084) ✅

Initier le paiement (après création de réservation)
Libérer l'escrow (après check-out)
Initier un remboursement (après annulation)


NOTIFICATION SERVICE (8086) ✅

Envoyer email de confirmation
Envoyer email d'annulation
Envoyer rappels de check-in/check-out



📥 REÇOIT DES MESSAGES DE :

USER SERVICE (8081) ✅

Réponse : Infos utilisateur (email, wallet, etc.)


PROPERTY SERVICE (8082) ✅

Réponse : Infos propriété (prix, disponibilité)
Réponse : Confirmation de blocage/déblocage de dates


PAYMENT SERVICE (8084) ✅

Événement : Paiement confirmé (txHash)
Événement : Escrow libéré
Événement : Remboursement effectué



## ----> Done i will add the messaging service com after
USER SERVICE (Port 8081)
📤 ENVOIE DES MESSAGES À :

❌ AUCUN (service autonome)

📥 REÇOIT DES MESSAGES DE :

BOOKING SERVICE (8083) ✅

Requête : Infos utilisateur
Réponse → Booking Service


MESSAGING SERVICE (8085) ⚠️ (optionnel)

Requête : Infos pour chat




PROPERTY SERVICE (Port 8082) - LISTING SERVICE
📤 ENVOIE DES MESSAGES À :

BLOCKCHAIN SERVICE (8089) ⚠️ (optionnel)

Enregistrer la propriété on-chain



📥 REÇOIT DES MESSAGES DE :

BOOKING SERVICE (8083) ✅

Requête : Infos propriété
Requête : Vérifier disponibilité
Commande : Bloquer dates
Commande : Débloquer dates
Réponse → Booking Service


## ---> Done ✅ 

PAYMENT SERVICE (Port 8084)
📤 ENVOIE DES MESSAGES À :

BLOCKCHAIN SERVICE (8089) ✅

Exécuter transaction blockchain
Libérer escrow via smart contract


BOOKING SERVICE (8083) ✅

Événement : Paiement confirmé


NOTIFICATION SERVICE (8086) ✅

Commande : Envoyer notification de paiement



📥 REÇOIT DES MESSAGES DE :

BOOKING SERVICE (8083) ✅

Commande : Initier paiement
Commande : Libérer escrow
Commande : Rembourser


BLOCKCHAIN SERVICE (8089) ✅

Événement : Transaction confirmée on-chain




MESSAGING SERVICE (Port 8085)
📤 ENVOIE DES MESSAGES À :

USER SERVICE (8081) ⚠️

Récupérer infos utilisateur pour le chat



📥 REÇOIT DES MESSAGES DE :

BOOKING SERVICE (8083) ⚠️ (optionnel)

Événement : Nouvelle réservation → Créer conversation


## --> Done ✅  Maybe i will add the review com later since it's optionnal 

NOTIFICATION SERVICE (Port 8086)
📤 ENVOIE DES MESSAGES À :

❌ AUCUN (service terminal - envoie juste des emails/SMS)

📥 REÇOIT DES MESSAGES DE :

BOOKING SERVICE (8083) ✅

Commande : Envoyer notification de réservation
Commande : Envoyer notification d'annulation


PAYMENT SERVICE (8084) ✅

Commande : Envoyer notification de paiement


REVIEW SERVICE (8087) ⚠️

Commande : Envoyer demande d'avis




REVIEW SERVICE (Port 8087)
📤 ENVOIE DES MESSAGES À :

NOTIFICATION SERVICE (8086) ⚠️

Demander d'envoyer email pour laisser un avis



📥 REÇOIT DES MESSAGES DE :

BOOKING SERVICE (8083) ⚠️

Événement : Réservation terminée → Permettre l'avis




MEDIA SERVICE (Port 8088)
📤 ENVOIE DES MESSAGES À :

❌ AUCUN (service autonome - upload/compression de photos)

📥 REÇOIT DES MESSAGES DE :

PROPERTY SERVICE (8082) ⚠️ (optionnel)

Requête : Stocker photos de propriété




BLOCKCHAIN SERVICE (Port 8089)
📤 ENVOIE DES MESSAGES À :

PAYMENT SERVICE (8084) ✅

Événement : Transaction confirmée on-chain



📥 REÇOIT DES MESSAGES DE :

PAYMENT SERVICE (8084) ✅

Commande : Exécuter transaction blockchain
Commande : Libérer escrow


PROPERTY SERVICE (8082) ⚠️ (optionnel)

Commande : Enregistrer propriété on-chain




📊 RÉSUMÉ VISUEL DES COMMUNICATIONS
┌─────────────────────────────────────────────────────────────┐
│                    BOOKING SERVICE (8083)                    │
│                   ★ CHEF D'ORCHESTRE ★                      │
└─────────────────────────────────────────────────────────────┘
        │ │ │ │
        │ │ │ └──────────────────┐
        │ │ │                    ▼
        │ │ │         ┌──────────────────────┐
        │ │ │         │ NOTIFICATION (8086)  │
        │ │ │         │ - Emails             │
        │ │ │         │ - SMS                │
        │ │ │         └──────────────────────┘
        │ │ │
        │ │ └─────────────────┐
        │ │                   ▼
        │ │         ┌──────────────────────┐
        │ │         │   PAYMENT (8084)     │◄────┐
        │ │         │ - Paiements          │     │
        │ │         │ - Escrow             │     │
        │ │         └──────────────────────┘     │
        │ │                   │                  │
        │ │                   └──────────────────┘
        │ │                   ▼                  
        │ │         ┌──────────────────────┐     
        │ │         │  BLOCKCHAIN (8089)   │     
        │ │         │ - Smart Contracts    │     
        │ │         └──────────────────────┘     
        │ │
        │ └──────────────┐
        │                ▼
        │      ┌──────────────────────┐
        │      │   PROPERTY (8082)    │
        │      │ - Propriétés         │
        │      │ - Disponibilités     │
        │      └──────────────────────┘
        │
        └─────────────┐
                      ▼
            ┌──────────────────────┐
            │    USER (8081)       │
            │ - Utilisateurs       │
            │ - Wallets            │
            └──────────────────────┘

✅ COMMUNICATIONS CRITIQUES (OBLIGATOIRES)

Booking ↔ User : Récupérer infos utilisateur
Booking ↔ Property : Vérifier dispo + bloquer dates
Booking → Payment : Initier paiements
Payment ↔ Blockchain : Exécuter transactions on-chain
Booking → Notification : Envoyer emails


⚠️ COMMUNICATIONS OPTIONNELLES

Booking → Messaging : Créer conversation après réservation
Booking → Review : Activer les avis après check-out
Property → Media : Stocker les photos
Property → Blockchain : Enregistrer propriété on-chain (si souhaité)