package com.rental.blockchain.service;

import com.rental.blockchain.contract.RentalPlatform;
import com.rental.blockchain.dto.BlockchainResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Service pour interagir avec le smart contract RentalPlatform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final Web3j web3j;
    private final Credentials adminCredentials;
    private final StaticGasProvider gasProvider;
    private final WalletService walletService;

    @Value("${ethereum.contract.address}")
    private String contractAddress;

    private RentalPlatform contract;

    /**
     * Initialisation: Charger le contrat
     */
    @PostConstruct
    public void init() {
        log.info("📄 Chargement du smart contract...");

        // ✅ CORRECTION 1: Valider l'adresse AVANT de charger
        if (!isValidAddress(contractAddress)) {
            log.error("❌ Adresse du contrat invalide: {}", contractAddress);
            throw new IllegalStateException(
                    "Contract address invalide dans application.yml: " + contractAddress
            );
        }

        log.info("📍 Adresse: {}", contractAddress);

        try {
            contract = RentalPlatform.load(
                    contractAddress,
                    web3j,
                    adminCredentials,
                    gasProvider
            );

            // ✅ CORRECTION 2: Tester la connexion au contrat
            if (contract.isValid()) {
                log.info("✅ Smart contract chargé avec succès!");

                // Test optionnel: vérifier qu'on peut lire du contrat
                try {
                    String deployedAddress = contract.getContractAddress();
                    log.info("✅ Contrat accessible à l'adresse: {}", deployedAddress);
                } catch (Exception e) {
                    log.warn("⚠️ Impossible de vérifier le contrat: {}", e.getMessage());
                }
            } else {
                log.error("❌ Smart contract invalide!");
                throw new IllegalStateException("Le contrat n'est pas valide");
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement du contrat: {}", e.getMessage(), e);
            throw new RuntimeException("Impossible de charger le smart contract", e);
        }
    }

    /**
     * ✅ CORRECTION 3: Valider le format de l'adresse
     */
    private boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        // Doit commencer par 0x et faire 42 caractères
        if (!address.startsWith("0x") || address.length() != 42) {
            log.error("❌ Format d'adresse invalide: {} (longueur: {})",
                    address, address.length());
            return false;
        }

        // Vérifier que c'est bien du hexadécimal après 0x
        String hexPart = address.substring(2);
        if (!hexPart.matches("[0-9a-fA-F]{40}")) {
            log.error("❌ L'adresse contient des caractères non-hexadécimaux");
            return false;
        }

        return true;
    }

    /**
     * Créer une réservation sur la blockchain
     */
    public BlockchainResponse createBooking(
            Long propertyId,
            Long startDate,
            Long endDate,
            BigDecimal rentalAmount,
            String userWalletPrivateKey
    ) {
        try {
            log.info("📦 Création réservation blockchain...");
            log.info("   Property: {}, Dates: {} -> {}, Amount: {} ETH",
                    propertyId, startDate, endDate, rentalAmount);

            // 1. Charger le wallet de l'utilisateur
            Credentials userCredentials = walletService.loadWallet(userWalletPrivateKey);
            log.info("👤 Utilisateur: {}", userCredentials.getAddress());

            // 2. Convertir le montant en Wei
            BigInteger rentalAmountWei = Convert.toWei(rentalAmount, Convert.Unit.ETHER)
                    .toBigInteger();

            // 3. Calculer le total (+ 5% frais)
            BigInteger platformFee = rentalAmountWei
                    .multiply(BigInteger.valueOf(5))
                    .divide(BigInteger.valueOf(100));
            BigInteger totalAmount = rentalAmountWei.add(platformFee);

            log.info("💰 Montants:");
            log.info("   Rental: {} wei", rentalAmountWei);
            log.info("   Fee (5%): {} wei", platformFee);
            log.info("   Total: {} wei", totalAmount);

            // 4. Charger le contrat avec le wallet user
            RentalPlatform contractWithUser = RentalPlatform.load(
                    contractAddress,
                    web3j,
                    userCredentials,
                    gasProvider
            );

            // 5. Appeler createBooking
            log.info("⏳ Envoi transaction au contrat: {}", contractAddress);

            TransactionReceipt receipt = contractWithUser.createBooking(
                    BigInteger.valueOf(propertyId),
                    BigInteger.valueOf(startDate),
                    BigInteger.valueOf(endDate),
                    rentalAmountWei,
                    totalAmount // value (ETH à envoyer)
            ).send();

            log.info("✅ Transaction confirmée!");
            log.info("   TX Hash: {}", receipt.getTransactionHash());
            log.info("   Block: {}", receipt.getBlockNumber());

            // 6. Extraire l'event BookingCreated
            List<RentalPlatform.BookingCreatedEventResponse> events =
                    contractWithUser.getBookingCreatedEvents(receipt);

            if (events.isEmpty()) {
                log.warn("⚠️ Event BookingCreated non trouvé dans la transaction");
                // Retourner quand même un succès avec ID 0
                return BlockchainResponse.success(
                        0L,
                        receipt.getTransactionHash(),
                        receipt.getBlockNumber().longValue()
                );
            }

            BigInteger blockchainBookingId = events.get(0).bookingId;

            log.info("🎉 Réservation créée! Blockchain ID: {}", blockchainBookingId);

            // 7. Retourner la réponse
            return BlockchainResponse.success(
                    blockchainBookingId.longValue(),
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue()
            );

        } catch (Exception e) {
            log.error("❌ Erreur createBooking: {}", e.getMessage(), e);
            return BlockchainResponse.error(e.getMessage());
        }
    }

    /**
     * Check-in
     */
    public BlockchainResponse checkIn(
            Long blockchainBookingId,
            String userWalletPrivateKey
    ) {
        try {
            log.info("🔑 Check-in pour réservation: {}", blockchainBookingId);

            Credentials userCredentials = walletService.loadWallet(userWalletPrivateKey);

            RentalPlatform contractWithUser = RentalPlatform.load(
                    contractAddress,
                    web3j,
                    userCredentials,
                    gasProvider
            );

            log.info("⏳ Envoi transaction check-in...");

            TransactionReceipt receipt = contractWithUser.checkIn(
                    BigInteger.valueOf(blockchainBookingId)
            ).send();

            log.info("✅ Check-in confirmé!");
            log.info("   TX Hash: {}", receipt.getTransactionHash());

            return BlockchainResponse.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue()
            );

        } catch (Exception e) {
            log.error("❌ Erreur checkIn: {}", e.getMessage(), e);
            return BlockchainResponse.error(e.getMessage());
        }
    }

    /**
     * Check-out
     */
    public BlockchainResponse checkOut(
            Long blockchainBookingId,
            String userWalletPrivateKey
    ) {
        try {
            log.info("🚪 Check-out pour réservation: {}", blockchainBookingId);

            Credentials userCredentials = walletService.loadWallet(userWalletPrivateKey);

            RentalPlatform contractWithUser = RentalPlatform.load(
                    contractAddress,
                    web3j,
                    userCredentials,
                    gasProvider
            );

            log.info("⏳ Envoi transaction check-out...");

            TransactionReceipt receipt = contractWithUser.checkOut(
                    BigInteger.valueOf(blockchainBookingId)
            ).send();

            log.info("✅ Check-out confirmé!");
            log.info("   TX Hash: {}", receipt.getTransactionHash());

            return BlockchainResponse.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue()
            );

        } catch (Exception e) {
            log.error("❌ Erreur checkOut: {}", e.getMessage(), e);
            return BlockchainResponse.error(e.getMessage());
        }
    }

    /**
     * Libérer les fonds (Admin seulement)
     */
    public BlockchainResponse releaseFunds(Long blockchainBookingId) {
        try {
            log.info("💰 Libération des fonds pour réservation: {}", blockchainBookingId);

            log.info("⏳ Envoi transaction...");

            TransactionReceipt receipt = contract.releaseFunds(
                    BigInteger.valueOf(blockchainBookingId)
            ).send();

            log.info("✅ Fonds libérés!");
            log.info("   TX Hash: {}", receipt.getTransactionHash());

            return BlockchainResponse.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue()
            );

        } catch (Exception e) {
            log.error("❌ Erreur releaseFunds: {}", e.getMessage(), e);
            return BlockchainResponse.error(e.getMessage());
        }
    }

    /**
     * Enregistrer un propriétaire (Admin seulement)
     */
    public BlockchainResponse setPropertyOwner(Long propertyId, String ownerAddress) {
        try {
            log.info("🏠 Enregistrement propriétaire: Property {}, Owner {}",
                    propertyId, ownerAddress);

            TransactionReceipt receipt = contract.setPropertyOwner(
                    BigInteger.valueOf(propertyId),
                    ownerAddress
            ).send();

            log.info("✅ Propriétaire enregistré!");

            return BlockchainResponse.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue()
            );

        } catch (Exception e) {
            log.error("❌ Erreur setPropertyOwner: {}", e.getMessage(), e);
            return BlockchainResponse.error(e.getMessage());
        }
    }

    /**
     * Obtenir une réservation (lecture, gratuit)
     */
    public RentalPlatform.Booking getBooking(Long blockchainBookingId) {
        try {
            return contract.getBooking(BigInteger.valueOf(blockchainBookingId)).send();
        } catch (Exception e) {
            log.error("❌ Erreur getBooking: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ CORRECTION 4: Retourner l'adresse avec validation
     */
    public String getContractAddress() {
        if (!isValidAddress(contractAddress)) {
            throw new IllegalStateException(
                    "Contract address invalide: " + contractAddress
            );
        }
        return contractAddress;
    }
}