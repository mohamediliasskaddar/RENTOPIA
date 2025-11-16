# 📋 INFORMATIONS DU CONTRAT DÉPLOYÉ

## 🔗 Contrat RentalPlatform

| Information | Valeur |
|-------------|--------|
| **Réseau** | Sepolia Testnet |
| **Adresse** | `0x4c5556c7bb47c8cadb5417af494AaE7792AF14d` |
| **Chain ID** | 11155111 |
| **Déployé le** | 14 Novembre 2025 |
| **Déployé par** | 0x34f528b67f1c31c2f579eef593ba537d63f1fd |
| **Etherscan** | [Voir le contrat](https://sepolia.etherscan.io/address/0x4c5556c7bb47c8cadb5417af494AaE7792AF14d) |

---

## ⚙️ Configuration Réseau

### RPC URL
```
https://sepolia.infura.io/v3/50e3d616c7ae4bb4bb5d48d97ab6d5a8
```

### Chain ID
```
11155111
```

### Explorer URL
```
https://sepolia.etherscan.io
```

---

## 📚 Fonctions du Contrat

### 1. `createBooking()` - Créer une réservation

**Paramètres:**
- `propertyId` (uint256): ID du logement
- `startDate` (uint256): Timestamp Unix de début
- `endDate` (uint256): Timestamp Unix de fin
- `rentalAmount` (uint256): Montant en Wei

**Payable:** ✅ OUI - `rentalAmount + 5%`

**Returns:** `bookingId` (uint256)

**Event émis:** `BookingCreated(bookingId, tenant, propertyId, totalAmount)`

---

### 2. `checkIn()` - Arrivée du locataire

**Paramètres:**
- `bookingId` (uint256): ID de la réservation

**Restrictions:**
- Seulement le locataire
- Après `startDate`
- Status = `CONFIRMED`

**Event émis:** `CheckInCompleted(bookingId, timestamp)`

---

### 3. `checkOut()` - Départ du locataire

**Paramètres:**
- `bookingId` (uint256): ID de la réservation

**Restrictions:**
- Seulement le locataire
- Status = `CHECKED_IN`

**Event émis:** `CheckOutCompleted(bookingId, timestamp)`

---

### 4. `releaseFunds()` - Libérer les fonds [ADMIN]

**Paramètres:**
- `bookingId` (uint256): ID de la réservation

**Restrictions:**
- ⚠️ ADMIN SEULEMENT
- Status = `CHECKED_OUT`

**Event émis:** `PaymentReleased(bookingId, owner, amount)`

---

### 5. `setPropertyOwner()` - Enregistrer propriétaire [ADMIN]

**Paramètres:**
- `propertyId` (uint256): ID du logement
- `owner` (address): Adresse du propriétaire

**⚠️ IMPORTANT:** À appeler AVANT toute réservation!

**Event émis:** `PropertyOwnerSet(propertyId, owner)`

---

### 6. Fonctions de Lecture (View - Gratuites)

```javascript
// Obtenir une réservation
const booking = await contract.getBooking(bookingId);

// Réservations d'un locataire
const bookingIds = await contract.getTenantBookings(tenantAddress);

// Réservations d'un logement
const bookingIds = await contract.getPropertyBookings(propertyId);

// Note moyenne (résultat * 100, ex: 467 = 4.67★)
const avgRating = await contract.getAverageRating(propertyId);

// Propriétaire d'un logement
const owner = await contract.getPropertyOwner(propertyId);

// Montant en attente de retrait
const pending = await contract.getPendingWithdrawal(ownerAddress);
```

---

## 🔔 Events du Contrat

### BookingCreated
```solidity
event BookingCreated(
    uint256 indexed bookingId,
    address indexed tenant,
    uint256 propertyId,
    uint256 totalAmount
);
```

### CheckInCompleted
```solidity
event CheckInCompleted(
    uint256 indexed bookingId,
    uint256 timestamp
);
```

### CheckOutCompleted
```solidity
event CheckOutCompleted(
    uint256 indexed bookingId,
    uint256 timestamp
);
```

### PaymentReleased
```solidity
event PaymentReleased(
    uint256 indexed bookingId,
    address indexed owner,
    uint256 amount
);
```

---

## 📊 Statuts des Réservations

```
0 = PENDING      (En attente)
1 = CONFIRMED    (Confirmée et payée)
2 = CHECKED_IN   (Locataire arrivé)
3 = CHECKED_OUT  (Locataire parti)
4 = COMPLETED    (Terminée, fonds libérés)
5 = CANCELLED    (Annulée)
```

**Cycle normal:** CONFIRMED → CHECKED_IN → CHECKED_OUT → COMPLETED

---

## 💰 Frais et Paiements

| Élément | Valeur |
|---------|--------|
| **Frais de plateforme** | 5% |
| **Wallet plateforme** | 0x34f528b67f1c31c2f579eef593ba537d63f1fd |
| **Escrow** | ✅ Activé |
| **Pull Pattern** | ✅ Activé |

---

## 🔒 Sécurité

- ✅ ReentrancyGuard
- ✅ Ownable (fonctions admin)
- ✅ Pull Pattern (retraits sécurisés)
- ✅ Checks-Effects-Interactions

---

## 📞 Contact

**Développeur Blockchain:** Ikrame Houzane

**Contrat:** https://sepolia.etherscan.io/address/0x4c5556c7bb47c8cadb5417af494AaE7792AF14d