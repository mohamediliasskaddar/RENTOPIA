// services/eventListener.js
const { ethers } = require('ethers');
const blockchainConfig = require('../config/blockchain.config');
const RentalPlatformABI = require('../abi/RentalPlatform.json');

/**
 * Service pour écouter les events émis par le smart contract
 *
 * ⚠️ IMPORTANT: Vous devez adapter ce fichier pour mettre à jour VOTRE base de données
 * Remplacez les console.log par vos requêtes MySQL
 */
class EventListener {
    constructor() {
        this.provider = new ethers.JsonRpcProvider(blockchainConfig.RPC_URL);
        this.contract = new ethers.Contract(
            blockchainConfig.CONTRACT_ADDRESS,
            RentalPlatformABI.abi,
            this.provider
        );
        this.isListening = false;
    }

    /**
     * Démarrer l'écoute des events
     */
    start() {
        if (this.isListening) {
            console.log('⚠️ Event Listener déjà actif');
            return;
        }

        console.log('🎧 Démarrage Event Listener...');
        console.log('📍 Contrat:', blockchainConfig.CONTRACT_ADDRESS);
        console.log('🌐 Réseau:', blockchainConfig.NETWORK);

        this.isListening = true;

        // Event: BookingCreated
        this.contract.on("BookingCreated", async (bookingId, tenant, propertyId, totalAmount, event) => {
            console.log('\n📢 ========================================');
            console.log('   EVENT: BookingCreated');
            console.log('========================================');
            console.log('Blockchain Booking ID:', Number(bookingId));
            console.log('Tenant:', tenant);
            console.log('Property ID:', Number(propertyId));
            console.log('Total Amount:', ethers.formatEther(totalAmount), 'ETH');
            console.log('Transaction Hash:', event.log.transactionHash);
            console.log('Block Number:', event.log.blockNumber);

            // ✅ TODO: Mettre à jour votre base de données MySQL
            /*
            try {
              await db.query(`
                UPDATE bookings
                SET status = 'CONFIRMED',
                    blockchain_booking_id = ?,
                    tx_hash = ?,
                    confirmed_at = NOW()
                WHERE tenant_address = ?
                  AND property_id = ?
                  AND status = 'PENDING'
                ORDER BY created_at DESC
                LIMIT 1
              `, [Number(bookingId), event.log.transactionHash, tenant, Number(propertyId)]);

              console.log('✅ MySQL mis à jour');
            } catch (error) {
              console.error('❌ Erreur mise à jour MySQL:', error);
            }
            */
        });

        // Event: CheckInCompleted
        this.contract.on("CheckInCompleted", async (bookingId, timestamp, event) => {
            console.log('\n📢 ========================================');
            console.log('   EVENT: CheckInCompleted');
            console.log('========================================');
            console.log('Blockchain Booking ID:', Number(bookingId));
            console.log('Timestamp:', Number(timestamp));
            console.log('Date:', new Date(Number(timestamp) * 1000).toLocaleString());
            console.log('Transaction Hash:', event.log.transactionHash);

            // ✅ TODO: Mettre à jour MySQL
            /*
            try {
              await db.query(`
                UPDATE bookings
                SET status = 'CHECKED_IN',
                    checkin_at = FROM_UNIXTIME(?)
                WHERE blockchain_booking_id = ?
              `, [Number(timestamp), Number(bookingId)]);

              console.log('✅ Check-in mis à jour dans MySQL');
            } catch (error) {
              console.error('❌ Erreur mise à jour check-in:', error);
            }
            */
        });

        // Event: CheckOutCompleted
        this.contract.on("CheckOutCompleted", async (bookingId, timestamp, event) => {
            console.log('\n📢 ========================================');
            console.log('   EVENT: CheckOutCompleted');
            console.log('========================================');
            console.log('Blockchain Booking ID:', Number(bookingId));
            console.log('Timestamp:', Number(timestamp));
            console.log('Date:', new Date(Number(timestamp) * 1000).toLocaleString());
            console.log('Transaction Hash:', event.log.transactionHash);

            // ✅ TODO: Mettre à jour MySQL
            /*
            try {
              await db.query(`
                UPDATE bookings
                SET status = 'CHECKED_OUT',
                    checkout_at = FROM_UNIXTIME(?)
                WHERE blockchain_booking_id = ?
              `, [Number(timestamp), Number(bookingId)]);

              console.log('✅ Check-out mis à jour dans MySQL');
            } catch (error) {
              console.error('❌ Erreur mise à jour check-out:', error);
            }
            */
        });

        // Event: PaymentReleased
        this.contract.on("PaymentReleased", async (bookingId, owner, amount, event) => {
            console.log('\n📢 ========================================');
            console.log('   EVENT: PaymentReleased');
            console.log('========================================');
            console.log('Blockchain Booking ID:', Number(bookingId));
            console.log('Owner:', owner);
            console.log('Amount:', ethers.formatEther(amount), 'ETH');
            console.log('Transaction Hash:', event.log.transactionHash);

            // ✅ TODO: Mettre à jour MySQL
            /*
            try {
              await db.query(`
                UPDATE bookings
                SET status = 'COMPLETED',
                    completed_at = NOW()
                WHERE blockchain_booking_id = ?
              `, [Number(bookingId)]);

              console.log('✅ Paiement libéré, réservation terminée');
            } catch (error) {
              console.error('❌ Erreur mise à jour paiement:', error);
            }
            */
        });

        // Event: BookingCancelled
        this.contract.on("BookingCancelled", async (bookingId, refundAmount, event) => {
            console.log('\n📢 ========================================');
            console.log('   EVENT: BookingCancelled');
            console.log('========================================');
            console.log('Blockchain Booking ID:', Number(bookingId));
            console.log('Refund Amount:', ethers.formatEther(refundAmount), 'ETH');
            console.log('Transaction Hash:', event.log.transactionHash);

            // ✅ TODO: Mettre à jour MySQL
            /*
            try {
              await db.query(`
                UPDATE bookings
                SET status = 'CANCELLED',
                    cancelled_at = NOW(),
                    refund_amount = ?
                WHERE blockchain_booking_id = ?
              `, [ethers.formatEther(refundAmount), Number(bookingId)]);

              console.log('✅ Annulation mise à jour dans MySQL');
            } catch (error) {
              console.error('❌ Erreur mise à jour annulation:', error);
            }
            */
        });

        // Event: PropertyOwnerSet
        this.contract.on("PropertyOwnerSet", async (propertyId, owner, event) => {
            console.log('\n📢 ========================================');
            console.log('   EVENT: PropertyOwnerSet');
            console.log('========================================');
            console.log('Property ID:', Number(propertyId));
            console.log('Owner:', owner);
            console.log('Transaction Hash:', event.log.transactionHash);
        });

        // Event: RatingAdded
        this.contract.on("RatingAdded", async (propertyId, tenant, stars, event) => {
            console.log('\n📢 ========================================');
            console.log('   EVENT: RatingAdded');
            console.log('========================================');
            console.log('Property ID:', Number(propertyId));
            console.log('Tenant:', tenant);
            console.log('Stars:', Number(stars));
            console.log('Transaction Hash:', event.log.transactionHash);

            // ✅ TODO: Mettre à jour MySQL
            /*
            try {
              await db.query(`
                INSERT INTO property_ratings (property_id, user_address, stars, tx_hash)
                VALUES (?, ?, ?, ?)
              `, [Number(propertyId), tenant, Number(stars), event.log.transactionHash]);

              console.log('✅ Note ajoutée dans MySQL');
            } catch (error) {
              console.error('❌ Erreur ajout note:', error);
            }
            */
        });

        console.log('✅ Event Listener actif et en écoute...\n');
    }

    /**
     * Arrêter l'écoute des events
     */
    stop() {
        if (!this.isListening) {
            console.log('⚠️ Event Listener n\'est pas actif');
            return;
        }

        this.contract.removeAllListeners();
        this.isListening = false;
        console.log('🛑 Event Listener arrêté');
    }

    /**
     * Vérifier si le listener est actif
     */
    isActive() {
        return this.isListening;
    }
}

module.exports = new EventListener();