# 🔗 Blockchain Integration - Rental Platform

## 📋 Description

Ce package contient tous les services nécessaires pour intégrer le smart contract RentalPlatform avec le backend.

## 🚀 Installation Rapide

### 1. Installer les dépendances

```bash
npm install ethers@6 dotenv
```

### 2. Configuration

Copie `.env.example` vers `.env` et remplis les variables:

```bash
cp .env.example .env
```

Édite `.env`:
```env
SEPOLIA_RPC_URL=https://sepolia.infura.io/v3/50e3d616c7ae4bb4bb5d48d97ab6d5a8
PRIVATE_KEY=votre_clef_privee_admin
CONTRACT_ADDRESS=0x4c5556c7bb47c8cadb5417af494AaE7792AF14d
CHAIN_ID=11155111
```

### 3. Intégration dans votre projet

Copiez le dossier `blockchain-integration/` dans votre backend:

```
backend/
├── microservices/
│   ├── booking/
│   ├── user/
│   └── properties/
└── shared/
    └── blockchain/  ← Collez ici le contenu de blockchain-integration/
```

---

## 📚 Utilisation

### Créer une réservation

```javascript
const contractService = require('./services/contractService');

const result = await contractService.createBooking(
  propertyId,        // uint256: ID du logement
  '2025-12-01',      // string: Date de début
  '2025-12-07',      // string: Date de fin
  1.5,               // number: Montant en ETH
  userWalletKey      // string: Clé privée du wallet user
);

console.log('Blockchain Booking ID:', result.blockchainBookingId);
console.log('Transaction Hash:', result.txHash);
console.log('Etherscan Link:', result.etherscanLink);
```

### Check-in

```javascript
await contractService.checkIn(
  blockchainBookingId,  // uint256: ID blockchain
  userWalletKey         // string: Clé privée du locataire
);
```

### Check-out

```javascript
await contractService.checkOut(
  blockchainBookingId,
  userWalletKey
);
```

### Libérer les fonds (Admin)

```javascript
await contractService.releaseFunds(
  blockchainBookingId
);
```

### Écouter les events blockchain

```javascript
const eventListener = require('./services/eventListener');

// Démarrer l'écoute
eventListener.start();

// Arrêter l'écoute
eventListener.stop();
```

---

## 🔔 Events Blockchain

Le contrat émet ces events automatiquement:

| Event | Quand | Action MySQL |
|-------|-------|--------------|
| `BookingCreated` | Réservation créée | UPDATE status = 'CONFIRMED' |
| `CheckInCompleted` | Locataire arrivé | UPDATE status = 'CHECKED_IN' |
| `CheckOutCompleted` | Locataire parti | UPDATE status = 'CHECKED_OUT' |
| `PaymentReleased` | Fonds libérés | UPDATE status = 'COMPLETED' |

---

## 🗃️ Structure MySQL Recommandée

Ajoutez ces colonnes à votre table `bookings`:

```sql
ALTER TABLE bookings
ADD COLUMN blockchain_booking_id INT UNSIGNED NULL,
ADD COLUMN tx_hash VARCHAR(66) NULL,
ADD COLUMN confirmed_at TIMESTAMP NULL,
ADD COLUMN checkin_at TIMESTAMP NULL,
ADD COLUMN checkout_at TIMESTAMP NULL,
ADD INDEX idx_blockchain_booking_id (blockchain_booking_id);
```

---

## 🔐 Gestion des Wallets

### ⚠️ IMPORTANT: Sécurité des clés privées

Dans un **environnement de production**, les utilisateurs doivent:
1. Utiliser **MetaMask** pour signer les transactions
2. **Jamais** envoyer leur clé privée au backend

**Pour ce projet (développement/test):**
- Le backend peut gérer des wallets temporaires
- Chaque utilisateur a une clé privée stockée (chiffrée) en BDD

### Créer un wallet pour un utilisateur

```javascript
const { ethers } = require('ethers');

// Créer un nouveau wallet
const wallet = ethers.Wallet.createRandom();

console.log('Address:', wallet.address);
console.log('Private Key:', wallet.privateKey);

// Sauvegarder en BDD (CHIFFRÉ!)
await db.query(`
  UPDATE users 
  SET wallet_address = ?, 
      wallet_private_key = AES_ENCRYPT(?, 'votre_secret_key')
  WHERE id = ?
`, [wallet.address, wallet.privateKey, userId]);
```

---

## 🧪 Tests

### Test de connexion

```javascript
const contractService = require('./services/contractService');

// Tester la connexion au contrat
const booking = await contractService.getBooking(1);
console.log('Booking:', booking);
```

---

## 📊 Flux Complet d'une Réservation

```
1. Frontend → POST /api/bookings/create
   ↓
2. Backend valide les données
   ↓
3. Backend crée entrée MySQL (status: PENDING)
   ↓
4. Backend appelle contractService.createBooking()
   ↓
5. Smart contract traite la réservation
   ↓
6. Event "BookingCreated" émis
   ↓
7. EventListener met à jour MySQL (status: CONFIRMED)
   ↓
8. Backend retourne au Frontend
```

---

**Liens utiles:**
- Contrat sur Etherscan: https://sepolia.etherscan.io/address/0x4c5556c7bb47c8cadb5417af494AaE7792AF14d
- Documentation Ethers.js: https://docs.ethers.org/v6/

---

## 📝 Notes Importantes

1. **Testnet Sepolia**: Ce contrat est déployé sur le testnet. Pas d'argent réel!
2. **Gas Fees**: Chaque transaction coûte du gas (ETH Sepolia gratuit)
3. **Confirmations**: Les transactions prennent 15-30 secondes
4. **Events**: Toujours écouter les events pour synchroniser MySQL

---

## 🔄 Versions

- **v1.0.0** (14 Nov 2025): Déploiement initial sur Sepolia