// config/blockchain.config.js
require('dotenv').config();

/**
 * Configuration de la blockchain Sepolia
 */
module.exports = {
    // Adresse du contrat RentalPlatform
    CONTRACT_ADDRESS: process.env.CONTRACT_ADDRESS || "0x4c5556c7bb47c8cadb5417af494AaE7792AF14d",

    // Réseau
    NETWORK: "sepolia",

    // URL RPC Infura
    RPC_URL: process.env.SEPOLIA_RPC_URL || "https://sepolia.infura.io/v3/50e3d616c7ae4bb4bb5d48d97ab6d5a8",

    // Chain ID
    CHAIN_ID: parseInt(process.env.CHAIN_ID) || 11155111,

    // Clé privée admin
    ADMIN_PRIVATE_KEY: process.env.PRIVATE_KEY,

    // Adresse admin
    ADMIN_ADDRESS: "0x34f528b67f1c31c2f579eef593ba537d63f1fd",

    // Explorateur
    ETHERSCAN_URL: process.env.ETHERSCAN_URL || "https://sepolia.etherscan.io",

    // Frais de plateforme
    PLATFORM_FEE_PERCENTAGE: 5,

    // Gas limit par défaut
    DEFAULT_GAS_LIMIT: 500000,

    // Confirmations à attendre
    CONFIRMATIONS: 1,

    // Timeout transactions (ms)
    TRANSACTION_TIMEOUT: 120000,

    /**
     * Lien Etherscan pour une transaction
     */
    getEtherscanTxLink(txHash) {
        return `${this.ETHERSCAN_URL}/tx/${txHash}`;
    },

    /**
     * Lien Etherscan pour une adresse
     */
    getEtherscanAddressLink(address) {
        return `${this.ETHERSCAN_URL}/address/${address}`;
    },

    /**
     * Valider la configuration
     */
    validate() {
        const required = ['SEPOLIA_RPC_URL', 'PRIVATE_KEY', 'CONTRACT_ADDRESS'];
        const missing = required.filter(key => !process.env[key]);

        if (missing.length > 0) {
            throw new Error(
                `Variables manquantes: ${missing.join(', ')}\n` +
                `Créez un .env basé sur .env.example`
            );
        }

        console.log('✅ Configuration blockchain validée');
    }
};