# 📦 Package d'Intégration Blockchain

Salut!

Voici le package pour intégrer le smart contract RentalPlatform avec notre backend.

## 📍 Informations du Contrat

- **Adresse:** `0x4c5556c7bb47c8cadb5417af494AaE7792AF14d`
- **Réseau:** Sepolia Testnet
- **Etherscan:** https://sepolia.etherscan.io/address/0x4c5556c7bb47c8cadb5417af494AaE7792AF14d
- **Déployé le:** 14 Novembre 2025

---

## 🚀 Installation Rapide

### 1. Copier le package

Copie le dossier `blockchain-integration/` dans ton backend:
```
backend/
├── microservices/
│   ├── booking/
│   ├── user/
│   └── properties/
└── shared/
    └── blockchain/  ← Colle ici tout le contenu de blockchain-integration/
```

### 2. Installer les dépendances
```bash
cd backend/shared/blockchain
npm install
```

### 3. Configurer l'environnement
```bash
cp .env.example .env
```

Puis édite `.env` avec ces valeurs:
```env
SEPOLIA_RPC_URL=https://sepolia.infura.io/v3/50e3d616c7ae4bb4bb5d48d97ab6d5a8
PRIVATE_KEY=demande_moi_la_clef_privee_admin
CONTRACT_ADDRESS=0x4c5556c7bb47c8cadb5417af494AaE7792AF14d
CHAIN_ID=11155111
```

⚠️ **Pour PRIVATE_KEY:** Demande-moi la clé privée admin en privé (ne pas mettre sur Git!)

---

## 📚 Documentation

### Fichiers à lire:

1. **`README.md`** - Guide d'utilisation complet avec exemples
2. **`CONTRACT_INFO.md`** - Détails du contrat et de toutes les fonctions
3. **`EXEMPLE_INTEGRATION.js`** - Exemples de code pour l'intégration

---

## 🔑 Utilisation Basique

### Créer une réservation
```javascript
const contractService = require('./services/contractService');

const result = await contractService.createBooking(
  propertyId,      // ID du logement
  '2025-12-01',    // Date début
  '2025-12-07',    // Date fin
  1.5,             // Montant en ETH
  userWalletKey    // Clé privée du wallet user
);

console.log('Blockchain ID:', result.blockchainBookingId);
console.log('Transaction:', result.txHash);
```

### Écouter les events blockchain
```javascript
const eventListener = require('./services/eventListener');

// Dans app.js ou server.js
eventListener.start();
```

Les events mettront à jour MySQL automatiquement (à configurer dans `eventListener.js`).

---

## 🗃️ Modifications MySQL Requises

Ajoute ces colonnes à la table `bookings`:
```sql
ALTER TABLE bookings
ADD COLUMN blockchain_booking_id INT UNSIGNED NULL COMMENT 'ID de la réservation sur blockchain',
ADD COLUMN tx_hash VARCHAR(66) NULL COMMENT 'Hash de la transaction',
ADD COLUMN confirmed_at TIMESTAMP NULL COMMENT 'Date confirmation blockchain',
ADD COLUMN checkin_at TIMESTAMP NULL COMMENT 'Date check-in',
ADD COLUMN checkout_at TIMESTAMP NULL COMMENT 'Date check-out',
ADD INDEX idx_blockchain_booking_id (blockchain_booking_id);
```

---

## 📊 Flux d'une Réservation
```
1. Frontend → POST /api/bookings/create
2. Backend crée entrée MySQL (status: PENDING)
3. Backend appelle contractService.createBooking()
4. Blockchain confirme → Event "BookingCreated"
5. EventListener met à jour MySQL (status: CONFIRMED)
6. Backend retourne au Frontend
```

---

**Liens utiles:**
- Contrat Etherscan: https://sepolia.etherscan.io/address/0x4c5556c7bb47c8cadb5417af494AaE7792AF14d
- Doc Ethers.js: https://docs.ethers.org/v6/

---

## ✅ Checklist d'Intégration

- [ ] Package copié dans `backend/shared/blockchain/`
- [ ] `npm install` effectué
- [ ] `.env` configuré avec les bonnes valeurs
- [ ] Colonnes MySQL ajoutées
- [ ] Lu le README.md
- [ ] Testé `contractService.getBooking(1)` (lecture simple)
- [ ] Event Listener démarré dans `server.js`
- [ ] Première réservation testée

---

## 📝 Notes Importantes

1. **Testnet Sepolia:** Pas d'argent réel, ETH gratuit sur faucets
2. **Gas Fees:** Chaque transaction coûte du gas (~0.002-0.005 ETH)
3. **Confirmations:** Les transactions prennent 15-30 secondes
4. **Wallets Users:** Chaque user doit avoir un wallet Ethereum (à créer/gérer)

---

Bon courage avec l'intégration! 🚀

N'hésite pas à me poser des questions!
```
