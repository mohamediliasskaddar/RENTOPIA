package com.rental.blockchain.controller;

import com.rental.blockchain.dto.*;
import com.rental.blockchain.service.ContractService;
import com.rental.blockchain.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import jakarta.validation.Valid;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST pour les interactions blockchain
 *
 * Endpoints disponibles:
 * - POST /api/blockchain/bookings/create
 * - POST /api/blockchain/bookings/{id}/checkin
 * - POST /api/blockchain/bookings/{id}/checkout
 * - POST /api/blockchain/bookings/{id}/release-funds
 * - POST /api/blockchain/wallets/create
 * - GET  /api/blockchain/health
 */
@Slf4j
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*") // Pour autoriser les appels depuis le frontend
public class BlockchainController {

    private final ContractService contractService;
    private final WalletService walletService;
    private final Web3j web3j;
    private final Credentials adminCredentials;

    @Value("${ethereum.network}")
    private String network;

    /**
     * GET /api/blockchain/health
     * Health check du service blockchain
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        log.info("📊 Health check demandé");

        try {
            // Tester la connexion Ethereum
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();

            HealthResponse response = HealthResponse.builder()
                    .status("UP")
                    .message("Blockchain Service is running")
                    .contractAddress(contractService.getContractAddress())
                    .network(network)
                    .currentBlock(blockNumber.longValue())
                    .adminWallet(adminCredentials.getAddress())
                    .build();

            log.info("✅ Health check OK - Block: {}, Admin: {}",
                    blockNumber, adminCredentials.getAddress());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Health check failed: {}", e.getMessage(), e);

            HealthResponse response = HealthResponse.builder()
                    .status("DOWN")
                    .message("Cannot connect to Ethereum: " + e.getMessage())
                    .contractAddress(contractService.getContractAddress())
                    .network(network)
                    .currentBlock(null)
                    .adminWallet(null)
                    .build();

            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * POST /api/blockchain/wallets/create
     * Créer un nouveau wallet Ethereum
     */
    @PostMapping("/wallets/create")
    public ResponseEntity<WalletResponse> createWallet() {
        log.info("📥 POST /api/blockchain/wallets/create");

        try {
            // 1. Créer le wallet
            Credentials credentials = walletService.createWallet();
            log.info("✅ Wallet créé: {}", credentials.getAddress());

            // 2. Obtenir la clé privée en hex avec padding correct
            String privateKeyHex = credentials.getEcKeyPair()
                    .getPrivateKey()
                    .toString(16);

            // Ajouter des zéros au début si nécessaire pour avoir exactement 64 caractères
            while (privateKeyHex.length() < 64) {
                privateKeyHex = "0" + privateKeyHex;
            }

            log.info("🔑 Private key length: {} caractères", privateKeyHex.length());

            // 3. Chiffrer la clé privée
            String encryptedKey = walletService.encryptPrivateKey(privateKeyHex);
            log.info("🔒 Clé privée chiffrée: {} caractères", encryptedKey.length());

            // 4. Créer la réponse
            WalletResponse response = WalletResponse.success(
                    credentials.getAddress(),
                    encryptedKey
            );

            log.info("✅ Wallet créé avec succès!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur création wallet: {}", e.getMessage(), e);

            WalletResponse response = WalletResponse.error(
                    "Erreur lors de la création du wallet: " + e.getMessage()
            );

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * POST /api/blockchain/bookings/create
     * Créer une réservation sur la blockchain
     */
    @PostMapping("/bookings/create")
    public ResponseEntity<BlockchainResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request
    ) {
        log.info("📥 POST /api/blockchain/bookings/create");
        log.info("   Property: {}, Amount: {} ETH",
                request.getPropertyId(), request.getRentalAmount());

        try {
            BlockchainResponse response = contractService.createBooking(
                    request.getPropertyId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getRentalAmount(),
                    request.getUserWalletPrivateKey()
            );

            if (response.isSuccess()) {
                log.info("✅ Réservation créée - ID: {}, TX: {}",
                        response.getBlockchainBookingId(), response.getTxHash());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ Erreur: {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/blockchain/bookings/{id}/checkin
     * Check-in pour une réservation
     */
    @PostMapping("/bookings/{id}/checkin")
    public ResponseEntity<BlockchainResponse> checkIn(
            @PathVariable Long id,
            @Valid @RequestBody CheckInRequest request
    ) {
        log.info("📥 POST /api/blockchain/bookings/{}/checkin", id);

        try {
            BlockchainResponse response = contractService.checkIn(
                    id,
                    request.getUserWalletPrivateKey()
            );

            if (response.isSuccess()) {
                log.info("✅ Check-in effectué - TX: {}", response.getTxHash());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ Erreur check-in: {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Exception check-in: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/blockchain/bookings/{id}/checkout
     * Check-out pour une réservation
     */
    @PostMapping("/bookings/{id}/checkout")
    public ResponseEntity<BlockchainResponse> checkOut(
            @PathVariable Long id,
            @Valid @RequestBody CheckOutRequest request
    ) {
        log.info("📥 POST /api/blockchain/bookings/{}/checkout", id);

        try {
            BlockchainResponse response = contractService.checkOut(
                    id,
                    request.getUserWalletPrivateKey()
            );

            if (response.isSuccess()) {
                log.info("✅ Check-out effectué - TX: {}", response.getTxHash());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ Erreur check-out: {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Exception check-out: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/blockchain/bookings/{id}/release-funds
     * Libérer les fonds (Admin seulement)
     */
    @PostMapping("/bookings/{id}/release-funds")
    public ResponseEntity<BlockchainResponse> releaseFunds(@PathVariable Long id) {
        log.info("📥 POST /api/blockchain/bookings/{}/release-funds", id);

        try {
            BlockchainResponse response = contractService.releaseFunds(id);

            if (response.isSuccess()) {
                log.info("✅ Fonds libérés - TX: {}", response.getTxHash());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ Erreur libération fonds: {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Exception libération fonds: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(BlockchainResponse.error(e.getMessage()));
        }
    }


    /**
     * GET /api/blockchain/blockchain-time
     * Obtenir l'heure actuelle de la blockchain
     */
    @GetMapping("/blockchain-time")
    public ResponseEntity<Map<String, Object>> getBlockchainTime() {
        try {
            // Récupérer le dernier block
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            EthBlock.Block block = web3j.ethGetBlockByNumber(
                    org.web3j.protocol.core.DefaultBlockParameter.valueOf(blockNumber),
                    false
            ).send().getBlock();

            // Timestamp en secondes
            BigInteger timestamp = block.getTimestamp();

            // Convertir en Date humaine
            Date date = new Date(timestamp.longValue() * 1000);

            Map<String, Object> response = new HashMap<>();
            response.put("blockchainTimestampSeconds", timestamp);
            response.put("blockchainTimestampMillis", timestamp.longValue() * 1000);
            response.put("blockchainTimeHuman", date.toString());
            response.put("localTimestampMillis", System.currentTimeMillis());
            response.put("localTimeHuman", new Date().toString());
            response.put("differenceSeconds",
                    (System.currentTimeMillis() / 1000) - timestamp.longValue());
            response.put("blockNumber", blockNumber);

            log.info("⏰ Temps blockchain: {} ({})", timestamp, date);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur récupération temps blockchain: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/blockchain/bookings/{id}
     * Obtenir les détails d'une réservation blockchain
     */
    @GetMapping("/bookings/{id}")
    public ResponseEntity<Map<String, Object>> getBookingDetails(@PathVariable Long id) {
        try {
            // Cette méthode dépend de ce qui existe dans ton ContractService
            // Si tu as une méthode getBooking() dans ContractService
            var booking = contractService.getBooking(id);

            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", id);
            response.put("details", booking);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/blockchain/bookings/reservation/{reservationId}
     * Obtenir l'ID blockchain à partir d'un reservationId
     */
    @GetMapping("/bookings/reservation/{reservationId}")
    public ResponseEntity<Map<String, Object>> getBlockchainBookingId(@PathVariable Long reservationId) {
        try {
            // Tu dois avoir une méthode dans ContractService ou une table en BDD
            // qui fait le mapping reservationId → blockchainBookingId
            // Pour l'instant, retourne juste l'info

            Map<String, Object> response = new HashMap<>();
            response.put("reservationId", reservationId);
            response.put("message", "Cette méthode nécessite une implémentation dans ContractService");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }

    }


    /**
     * POST /api/blockchain/properties/set-owner
     * Enregistrer un propriétaire pour un propertyId (Admin seulement)
     */
    @PostMapping("/properties/set-owner")
    public ResponseEntity<BlockchainResponse> setPropertyOwner(
            @Valid @RequestBody SetPropertyOwnerRequest request
    ) {
        log.info("📥 POST /api/blockchain/properties/set-owner");
        log.info("   Property: {}, Owner: {}",
                request.getPropertyId(), request.getOwnerAddress());

        try {
            BlockchainResponse response = contractService.setPropertyOwner(
                    request.getPropertyId(),
                    request.getOwnerAddress()
            );

            if (response.isSuccess()) {
                log.info("✅ Propriétaire enregistré - TX: {}", response.getTxHash());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ Erreur: {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(BlockchainResponse.error(e.getMessage()));
        }
    }
}