
new 

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

collection postman (communication microservices )

https://mina26b-7039393.postman.co/workspace/Mina's-Workspace~b4d122bc-2b17-4bfb-86c4-caf4a92d9a4a/collection/49591630-c76e0b54-5367-49a4-8332-857e8569df01?action=share&source=copy-link&creator=49591630 


https://mina26b-7039393.postman.co/workspace/Mina's-Workspace~b4d122bc-2b17-4bfb-86c4-caf4a92d9a4a/folder/49591630-6f7e9a29-e989-4539-aa68-3c01495a48ca?action=share&source=copy-link&creator=49591630&ctx=documentation 

(don't test any request related to listing service(communication)
m still working on it ,this version is not final 
it's just for front end dev )