GET par ID ✅
http
GET http://localhost:8083/bookings/{id}
Exemple : GET http://localhost:8083/bookings/1

2. GET mes réservations ✅
   http
   GET http://localhost:8083/bookings/user/me
   (Requiert authentification)

3. GET par propriété ✅
   http
   GET http://localhost:8083/bookings/property/{propertyId}
   Exemple : GET http://localhost:8083/bookings/property/1

4. HEALTH CHECK ✅
   http
   GET http://localhost:8083/bookings/health
5. ### ✅ EXISTE - Annulation (vous avez cancel, pas refund)
PATCH http://localhost:8083/bookings/1/cancel?reason=test

### ✅ EXISTE - Libération escrow
PATCH http://localhost:8083/bookings/1/release-escrow?txHash=abc123

### ✅ EXISTE - Confirmation
PATCH http://localhost:8083/bookings/1/confirm?blockchainTxHash=xyz789

### ✅ EXISTE - Check-in
PATCH http://localhost:8083/bookings/1/check-in

### ✅ EXISTE - Check-out
PATCH http://localhost:8083/bookings/1/check-out

🔄 Flux de réservation
text
1. Création Demande → POST /bookings
   ↓
2. Vérification disponibilité (Listing Service)
   ↓
3. Calcul prix (PropertyVersion + Pricing)
   ↓
4. Création réservation PENDING
   ↓
5. Paiement blockchain (Payment Service)
   ↓
6. Confirmation → PATCH /bookings/{id}/confirm
   ↓
7. Notification (Notification Service)
   ↓
8. Check-in/Check-out