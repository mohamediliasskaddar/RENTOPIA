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
import java.math.BigInteger;

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
}