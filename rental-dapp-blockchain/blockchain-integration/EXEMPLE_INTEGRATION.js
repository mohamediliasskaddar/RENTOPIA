// EXEMPLE_INTEGRATION.js
// Exemple d'intégration dans le microservice Booking

const contractService = require('./services/contractService');
const eventListener = require('./services/eventListener');

/**
 * EXEMPLE 1: Créer une réservation
 */
async function exempleCreerReservation() {
    try {
        console.log('\n🔷 EXEMPLE 1: Créer une réservation\n');

        // Données de la réservation
        const propertyId = 1;
        const startDate = '2025-12-01';
        const endDate = '2025-12-07';
        const rentalAmount = 1.5; // ETH

        // Clé privée du wallet utilisateur (depuis BDD, décryptée)
        const userWallet = '0x1234...'; // À récupérer depuis MySQL

        // Appeler la blockchain
        const result = await contractService.createBooking(
            propertyId,
            startDate,
            endDate,
            rentalAmount,
            userWallet
        );

        console.log('✅ Réservation créée!');
        console.log('Blockchain ID:', result.blockchainBookingId);
        console.log('Transaction:', result.txHash);
        console.log('Etherscan:', result.etherscanLink);

        // Mettre à jour MySQL
        /*
        await db.query(`
          UPDATE bookings
          SET blockchain_booking_id = ?,
              tx_hash = ?,
              status = 'CONFIRMED'
          WHERE id = ?
        `, [result.blockchainBookingId, result.txHash, mysqlBookingId]);
        */

    } catch (error) {
        console.error('❌ Erreur:', error.message);
    }
}

/**
 * EXEMPLE 2: Check-in
 */
async function exempleCheckIn() {
    try {
        console.log('\n🔷 EXEMPLE 2: Check-in\n');

        const blockchainBookingId = 5; // Récupéré depuis MySQL
        const userWallet = '0x1234...'; // Depuis BDD

        const result = await contractService.checkIn(
            blockchainBookingId,
            userWallet
        );

        console.log('✅ Check-in confirmé!');
        console.log('Transaction:', result.txHash);

    } catch (error) {
        console.error('❌ Erreur:', error.message);
    }
}

/**
 * EXEMPLE 3: Check-out
 */
async function exempleCheckOut() {
    try {
        console.log('\n🔷 EXEMPLE 3: Check-out\n');

        const blockchainBookingId = 5;
        const userWallet = '0x1234...';

        const result = await contractService.checkOut(
            blockchainBookingId,
            userWallet
        );

        console.log('✅ Check-out confirmé!');
        console.log('Transaction:', result.txHash);

    } catch (error) {
        console.error('❌ Erreur:', error.message);
    }
}

/**
 * EXEMPLE 4: Libérer les fonds (Admin)
 */
async function exempleReleaseFunds() {
    try {
        console.log('\n🔷 EXEMPLE 4: Libérer les fonds\n');

        const blockchainBookingId = 5;

        // Seul l'admin peut appeler cette fonction
        const result = await contractService.releaseFunds(blockchainBookingId);

        console.log('✅ Fonds libérés!');
        console.log('Transaction:', result.txHash);

    } catch (error) {
        console.error('❌ Erreur:', error.message);
    }
}

/**
 * EXEMPLE 5: Enregistrer un propriétaire
 */
async function exempleSetPropertyOwner() {
    try {
        console.log('\n🔷 EXEMPLE 5: Enregistrer un propriétaire\n');

        const propertyId = 1;
        const ownerAddress = '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb';

        const result = await contractService.setPropertyOwner(
            propertyId,
            ownerAddress
        );

        console.log('✅ Propriétaire enregistré!');
        console.log('Transaction:', result.txHash);

    } catch (error) {
        console.error('❌ Erreur:', error.message);
    }
}

/**
 * EXEMPLE 6: Lire les données (gratuit, pas de transaction)
 */
async function exempleLectureDonnees() {
    try {
        console.log('\n🔷 EXEMPLE 6: Lire les données\n');

        // Obtenir une réservation
        const booking = await contractService.getBooking(5);
        console.log('📋 Réservation:', booking);

        // Obtenir les réservations d'un locataire
        const tenantBookings = await contractService.getTenantBookings(
            '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
        );
        console.log('📋 Réservations du locataire:', tenantBookings);

        // Obtenir le solde en attente d'un propriétaire
        const pending = await contractService.getPendingWithdrawal(
            '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
        );
        console.log('💰 Solde en attente:', pending, 'ETH');

    } catch (error) {
        console.error('❌ Erreur:', error.message);
    }
}

/**
 * EXEMPLE 7: Démarrer l'écoute des events
 */
function exempleDemarrerEventListener() {
    console.log('\n🔷 EXEMPLE 7: Démarrer Event Listener\n');

    // Démarrer l'écoute
    eventListener.start();

    console.log('✅ Event Listener actif!');
    console.log('Le listener va afficher les events dans la console');
    console.log('Il mettra à jour MySQL automatiquement (si configuré)');

    // Arrêter après 1 heure (exemple)
    setTimeout(() => {
        eventListener.stop();
        console.log('🛑 Event Listener arrêté');
    }, 3600000);
}

/**
 * EXEMPLE 8: Intégration complète dans une API Route
 */
function exempleAPIRoute() {
    console.log('\n🔷 EXEMPLE 8: Intégration dans API Route\n');

    const code = `
// routes/bookings.js
const express = require('express');
const router = express.Router();
const contractService = require('../shared/blockchain/services/contractService');
const db = require('../database/connection');

/**
 * POST /api/bookings/create
 * Créer une nouvelle réservation
 */
router.post('/create', async (req, res) => {
  try {
    const { propertyId, startDate, endDate, rentalAmount } = req.body;
    const userId = req.user.id; // Depuis authentification
    
    // 1. Valider les données
    if (!propertyId || !startDate || !endDate || !rentalAmount) {
      return res.status(400).json({ error: 'Données manquantes' });
    }
    
    // 2. Vérifier disponibilité en BDD
    const [existing] = await db.query(\`
      SELECT * FROM bookings 
      WHERE property_id = ? 
      AND status IN ('CONFIRMED', 'CHECKED_IN')
      AND (
        (start_date <= ? AND end_date >= ?) OR
        (start_date <= ? AND end_date >= ?)
      )
    \`, [propertyId, startDate, startDate, endDate, endDate]);
    
    if (existing.length > 0) {
      return res.status(400).json({ error: 'Dates non disponibles' });
    }
    
    // 3. Créer entrée PENDING en BDD
    const [result] = await db.query(\`
      INSERT INTO bookings (user_id, property_id, start_date, end_date, amount, status)
      VALUES (?, ?, ?, ?, ?, 'PENDING')
    \`, [userId, propertyId, startDate, endDate, rentalAmount]);
    
    const mysqlBookingId = result.insertId;
    
    // 4. Récupérer le wallet de l'utilisateur (décrypté)
    const [user] = await db.query(\`
      SELECT AES_DECRYPT(wallet_private_key, ?) as wallet
      FROM users WHERE id = ?
    \`, [process.env.WALLET_ENCRYPTION_KEY, userId]);
    
    const userWallet = user[0].wallet.toString();
    
    // 5. Appeler la blockchain
    const blockchainResult = await contractService.createBooking(
      propertyId,
      startDate,
      endDate,
      rentalAmount,
      userWallet
    );
    
    // 6. Mettre à jour MySQL avec les infos blockchain
    await db.query(\`
      UPDATE bookings 
      SET blockchain_booking_id = ?,
          tx_hash = ?,
          status = 'CONFIRMED',
          confirmed_at = NOW()
      WHERE id = ?
    \`, [blockchainResult.blockchainBookingId, blockchainResult.txHash, mysqlBookingId]);
    
    // 7. Répondre au frontend
    res.json({
      success: true,
      bookingId: mysqlBookingId,
      blockchainId: blockchainResult.blockchainBookingId,
      txHash: blockchainResult.txHash,
      etherscanLink: blockchainResult.etherscanLink
    });
    
  } catch (error) {
    console.error('Erreur création réservation:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
  `;

    console.log(code);
}

// Exporter les fonctions
module.exports = {
    exempleCreerReservation,
    exempleCheckIn,
    exempleCheckOut,
    exempleReleaseFunds,
    exempleSetPropertyOwner,
    exempleLectureDonnees,
    exempleDemarrerEventListener,
    exempleAPIRoute
};

// Si lancé directement
if (require.main === module) {
    console.log('📚 EXEMPLES D\'INTÉGRATION BLOCKCHAIN');
    console.log('=====================================\n');
    console.log('Décommentez la fonction que vous voulez tester:\n');

    // Décommentez pour tester:
    // exempleCreerReservation();
    // exempleCheckIn();
    // exempleCheckOut();
    // exempleReleaseFunds();
    // exempleSetPropertyOwner();
    // exempleLectureDonnees();
    // exempleDemarrerEventListener();
    exempleAPIRoute();
}