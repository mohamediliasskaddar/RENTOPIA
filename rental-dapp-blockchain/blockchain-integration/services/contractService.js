// services/contractService.js
const { ethers } = require('ethers');
const blockchainConfig = require('../config/blockchain.config');
const RentalPlatformABI = require('../abi/RentalPlatform.json');

/**
 * Service pour interagir avec le smart contract RentalPlatform
 */
class ContractService {
    constructor() {
        // Connexion au réseau Sepolia
        this.provider = new ethers.JsonRpcProvider(blockchainConfig.RPC_URL);

        // Wallet admin
        this.adminWallet = new ethers.Wallet(
            blockchainConfig.ADMIN_PRIVATE_KEY,
            this.provider
        );

        // Instance du contrat (lecture seule)
        this.contract = new ethers.Contract(
            blockchainConfig.CONTRACT_ADDRESS,
            RentalPlatformABI.abi,
            this.provider
        );

        // Instance avec signer (pour écrire)
        this.contractWithSigner = new ethers.Contract(
            blockchainConfig.CONTRACT_ADDRESS,
            RentalPlatformABI.abi,
            this.adminWallet
        );
    }

    /**
     * Créer une réservation sur la blockchain
     *
     * @param {number} propertyId - ID du logement
     * @param {string} startDate - Date de début (YYYY-MM-DD)
     * @param {string} endDate - Date de fin (YYYY-MM-DD)
     * @param {number} rentalAmount - Montant en ETH
     * @param {string} userWallet - Clé privée du wallet user
     * @returns {Promise<Object>} Résultat avec blockchainBookingId et txHash
     */
    async createBooking(propertyId, startDate, endDate, rentalAmount, userWallet) {
        try {
            console.log('📦 Création réservation blockchain...', {
                propertyId,
                startDate,
                endDate,
                rentalAmount
            });

            // Convertir les dates en timestamps Unix
            const startTimestamp = Math.floor(new Date(startDate).getTime() / 1000);
            const endTimestamp = Math.floor(new Date(endDate).getTime() / 1000);

            // Convertir le montant en Wei
            const rentalAmountWei = ethers.parseEther(rentalAmount.toString());

            // Calculer le total (montant + 5% frais)
            const platformFee = rentalAmountWei * 5n / 100n;
            const totalAmount = rentalAmountWei + platformFee;

            console.log('💰 Montants:', {
                rentalAmount: ethers.formatEther(rentalAmountWei) + ' ETH',
                platformFee: ethers.formatEther(platformFee) + ' ETH',
                totalAmount: ethers.formatEther(totalAmount) + ' ETH'
            });

            // Créer un wallet pour l'utilisateur
            const userSigner = new ethers.Wallet(userWallet, this.provider);
            const contractWithUserSigner = this.contract.connect(userSigner);

            // Appeler createBooking
            console.log('⏳ Envoi de la transaction...');
            const tx = await contractWithUserSigner.createBooking(
                propertyId,
                startTimestamp,
                endTimestamp,
                rentalAmountWei,
                { value: totalAmount }
            );

            console.log('✅ Transaction envoyée:', tx.hash);
            console.log('⏳ Attente de confirmation...');

            // Attendre la confirmation
            const receipt = await tx.wait();

            console.log('✅ Transaction confirmée dans le bloc:', receipt.blockNumber);

            // Extraire bookingId depuis l'event
            const bookingCreatedEvent = receipt.logs
                .map(log => {
                    try {
                        return this.contract.interface.parseLog(log);
                    } catch {
                        return null;
                    }
                })
                .find(e => e && e.name === "BookingCreated");

            if (!bookingCreatedEvent) {
                throw new Error("Event BookingCreated non trouvé");
            }

            const blockchainBookingId = bookingCreatedEvent.args.bookingId;

            console.log('🎉 Réservation créée avec succès!', {
                blockchainBookingId: Number(blockchainBookingId),
                txHash: tx.hash
            });

            return {
                success: true,
                blockchainBookingId: Number(blockchainBookingId),
                txHash: tx.hash,
                blockNumber: receipt.blockNumber,
                etherscanLink: blockchainConfig.getEtherscanTxLink(tx.hash)
            };

        } catch (error) {
            console.error('❌ Erreur createBooking:', error.message);
            throw error;
        }
    }

    /**
     * Check-in
     */
    async checkIn(blockchainBookingId, userWallet) {
        try {
            console.log('🔑 Check-in pour réservation:', blockchainBookingId);

            const userSigner = new ethers.Wallet(userWallet, this.provider);
            const contractWithUserSigner = this.contract.connect(userSigner);

            const tx = await contractWithUserSigner.checkIn(blockchainBookingId);
            console.log('⏳ Transaction envoyée:', tx.hash);

            const receipt = await tx.wait();
            console.log('✅ Check-in confirmé!');

            return {
                success: true,
                txHash: tx.hash,
                blockNumber: receipt.blockNumber,
                etherscanLink: blockchainConfig.getEtherscanTxLink(tx.hash)
            };
        } catch (error) {
            console.error('❌ Erreur checkIn:', error.message);
            throw error;
        }
    }

    /**
     * Check-out
     */
    async checkOut(blockchainBookingId, userWallet) {
        try {
            console.log('🚪 Check-out pour réservation:', blockchainBookingId);

            const userSigner = new ethers.Wallet(userWallet, this.provider);
            const contractWithUserSigner = this.contract.connect(userSigner);

            const tx = await contractWithUserSigner.checkOut(blockchainBookingId);
            console.log('⏳ Transaction envoyée:', tx.hash);

            const receipt = await tx.wait();
            console.log('✅ Check-out confirmé!');

            return {
                success: true,
                txHash: tx.hash,
                blockNumber: receipt.blockNumber,
                etherscanLink: blockchainConfig.getEtherscanTxLink(tx.hash)
            };
        } catch (error) {
            console.error('❌ Erreur checkOut:', error.message);
            throw error;
        }
    }

    /**
     * Libérer les fonds (Admin seulement)
     */
    async releaseFunds(blockchainBookingId) {
        try {
            console.log('💰 Libération des fonds pour réservation:', blockchainBookingId);

            const tx = await this.contractWithSigner.releaseFunds(blockchainBookingId);
            console.log('⏳ Transaction envoyée:', tx.hash);

            const receipt = await tx.wait();
            console.log('✅ Fonds libérés!');

            return {
                success: true,
                txHash: tx.hash,
                blockNumber: receipt.blockNumber,
                etherscanLink: blockchainConfig.getEtherscanTxLink(tx.hash)
            };
        } catch (error) {
            console.error('❌ Erreur releaseFunds:', error.message);
            throw error;
        }
    }

    /**
     * Enregistrer un propriétaire (Admin seulement)
     */
    async setPropertyOwner(propertyId, ownerAddress) {
        try {
            console.log('🏠 Enregistrement propriétaire:', {
                propertyId,
                ownerAddress
            });

            const tx = await this.contractWithSigner.setPropertyOwner(
                propertyId,
                ownerAddress
            );
            console.log('⏳ Transaction envoyée:', tx.hash);

            const receipt = await tx.wait();
            console.log('✅ Propriétaire enregistré!');

            return {
                success: true,
                txHash: tx.hash,
                etherscanLink: blockchainConfig.getEtherscanTxLink(tx.hash)
            };
        } catch (error) {
            console.error('❌ Erreur setPropertyOwner:', error.message);
            throw error;
        }
    }

    /**
     * Obtenir les détails d'une réservation
     */
    async getBooking(blockchainBookingId) {
        try {
            const booking = await this.contract.getBooking(blockchainBookingId);

            return {
                bookingId: Number(booking.bookingId),
                tenant: booking.tenant,
                propertyId: Number(booking.propertyId),
                startDate: Number(booking.startDate),
                endDate: Number(booking.endDate),
                rentalAmount: ethers.formatEther(booking.rentalAmount),
                platformFee: ethers.formatEther(booking.platformFee),
                status: Number(booking.status),
                checkInTime: Number(booking.checkInTime),
                checkOutTime: Number(booking.checkOutTime)
            };
        } catch (error) {
            console.error('❌ Erreur getBooking:', error.message);
            throw error;
        }
    }

    /**
     * Obtenir les réservations d'un locataire
     */
    async getTenantBookings(tenantAddress) {
        try {
            const bookingIds = await this.contract.getTenantBookings(tenantAddress);
            return bookingIds.map(id => Number(id));
        } catch (error) {
            console.error('❌ Erreur getTenantBookings:', error.message);
            throw error;
        }
    }

    /**
     * Obtenir le solde en attente d'un propriétaire
     */
    async getPendingWithdrawal(ownerAddress) {
        try {
            const amount = await this.contract.getPendingWithdrawal(ownerAddress);
            return ethers.formatEther(amount);
        } catch (error) {
            console.error('❌ Erreur getPendingWithdrawal:', error.message);
            throw error;
        }
    }
}

module.exports = new ContractService();