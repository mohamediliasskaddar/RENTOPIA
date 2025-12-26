package com.rental.payment.controller;

import com.rental.payment.dto.CreatePaymentDTO;
import com.rental.payment.dto.PaymentResponse;
import com.rental.payment.entity.BlockchainTransaction;
import com.rental.payment.repository.BlockchainTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Controller SIMPLIFIÉ pour Booking Service
 * Version mock sans blockchain réelle
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final BlockchainTransactionRepository transactionRepository;

    /**
     * ✅ ENDPOINT 1 : Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service");
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ ENDPOINT 2 : Créer un paiement
     * POST /api/v1/payments/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentDTO dto) {
        log.info("📝 Création paiement pour réservation #{}", dto.getReservationId());

        try {
            // Validation
            if (dto.getReservationId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "reservationId requis"));
            }
            if (dto.getPayerWallet() == null || dto.getPayerWallet().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "payerWallet requis"));
            }
            if (dto.getPayeeWallet() == null || dto.getPayeeWallet().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "payeeWallet requis"));
            }
            if (dto.getAmountEth() == null || dto.getAmountEth() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "amountEth doit être > 0"));
            }

            // Créer un hash mock
            String mockTxHash = "0x" + System.currentTimeMillis() + "mock";

            BlockchainTransaction transaction = BlockchainTransaction.builder()
                    .reservationId(dto.getReservationId())
                    .payerWalletAddress(dto.getPayerWallet())
                    .payeeWalletAddress(dto.getPayeeWallet())
                    .amountEth(dto.getAmountEth())
                    .gasFeeEth(0.001)
                    .transactionHash(mockTxHash)
                    .paymentType(BlockchainTransaction.PaymentType.BOOKING_PAYMENT)
                    .paymentStatus(BlockchainTransaction.PaymentStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now())
                    .confirmedAt(LocalDateTime.now())
                    .blockNumber(12345678)
                    .build();

            transaction = transactionRepository.save(transaction);
            log.info("✅ Paiement créé avec ID: {}", transaction.getId());

            PaymentResponse response = PaymentResponse.fromEntity(transaction);
            response.setMessage("Paiement créé avec succès");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ Erreur création paiement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur création paiement: " + e.getMessage()));
        }
    }

    /**
     * ✅ ENDPOINT 3 : Récupérer un paiement par réservation
     * GET /api/v1/payments/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> getPaymentByReservation(@PathVariable Integer reservationId) {
        log.info("🔍 Recherche paiement pour réservation #{}", reservationId);

        try {
            BlockchainTransaction transaction = transactionRepository
                    .findByReservationIdAndPaymentType(
                            reservationId,
                            BlockchainTransaction.PaymentType.BOOKING_PAYMENT
                    )
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé pour cette réservation"));

            PaymentResponse response = PaymentResponse.fromEntity(transaction);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur récupération paiement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ ENDPOINT 4 : Vérifier le statut d'une transaction
     * GET /api/v1/payments/transaction/{txHash}/status
     */
    @GetMapping("/transaction/{txHash}/status")
    public ResponseEntity<?> getTransactionStatus(@PathVariable String txHash) {
        log.info("🔍 Vérification statut transaction {}", txHash);

        try {
            BlockchainTransaction transaction = transactionRepository.findByTransactionHash(txHash)
                    .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

            return ResponseEntity.ok(transaction.getPaymentStatus().name());

        } catch (Exception e) {
            log.error("❌ Erreur vérification statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ ENDPOINT 5 : Libérer l'escrow
     * POST /api/v1/payments/escrow/release
     */
    @PostMapping("/escrow/release")
    public ResponseEntity<?> releaseEscrow(
            @RequestParam Integer reservationId,
            @RequestParam String hostWallet
    ) {
        log.info("📝 Libération escrow pour réservation #{} vers {}", reservationId, hostWallet);

        try {
            // Récupérer le paiement initial
            BlockchainTransaction originalPayment = transactionRepository
                    .findByReservationIdAndPaymentType(
                            reservationId,
                            BlockchainTransaction.PaymentType.BOOKING_PAYMENT
                    )
                    .orElseThrow(() -> new RuntimeException("Paiement initial non trouvé"));

            // Créer une transaction de libération mock
            String mockTxHash = "0x" + System.currentTimeMillis() + "release";

            BlockchainTransaction releaseTransaction = BlockchainTransaction.builder()
                    .reservationId(reservationId)
                    .payerWalletAddress("ESCROW_CONTRACT")
                    .payeeWalletAddress(hostWallet)
                    .amountEth(originalPayment.getAmountEth() * 0.95)
                    .gasFeeEth(0.001)
                    .transactionHash(mockTxHash)
                    .paymentType(BlockchainTransaction.PaymentType.ESCROW_RELEASE)
                    .paymentStatus(BlockchainTransaction.PaymentStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now())
                    .confirmedAt(LocalDateTime.now())
                    .blockNumber(12345679)
                    .build();

            transactionRepository.save(releaseTransaction);
            log.info("✅ Escrow libéré avec txHash: {}", mockTxHash);

            return ResponseEntity.ok(Map.of("message", "Escrow libéré avec succès", "txHash", mockTxHash));

        } catch (Exception e) {
            log.error("❌ Erreur libération escrow: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur libération escrow: " + e.getMessage()));
        }
    }

    /**
     * ✅ ENDPOINT 6 : Initier un remboursement
     * POST /api/v1/payments/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<?> initiateRefund(
            @RequestParam Integer reservationId,
            @RequestParam String reason
    ) {
        log.info("📝 Remboursement pour réservation #{} - Raison: {}", reservationId, reason);

        try {
            BlockchainTransaction originalPayment = transactionRepository
                    .findByReservationIdAndPaymentType(
                            reservationId,
                            BlockchainTransaction.PaymentType.BOOKING_PAYMENT
                    )
                    .orElseThrow(() -> new RuntimeException("Paiement initial non trouvé"));

            String mockTxHash = "0x" + System.currentTimeMillis() + "refund";

            BlockchainTransaction refundTransaction = BlockchainTransaction.builder()
                    .reservationId(reservationId)
                    .payerWalletAddress("ESCROW_CONTRACT")
                    .payeeWalletAddress(originalPayment.getPayerWalletAddress())
                    .amountEth(originalPayment.getAmountEth())
                    .gasFeeEth(0.001)
                    .transactionHash(mockTxHash)
                    .paymentType(BlockchainTransaction.PaymentType.REFUND)
                    .paymentStatus(BlockchainTransaction.PaymentStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now())
                    .confirmedAt(LocalDateTime.now())
                    .blockNumber(12345680)
                    .build();

            transactionRepository.save(refundTransaction);
            log.info("✅ Remboursement effectué avec txHash: {}", mockTxHash);

            return ResponseEntity.ok(Map.of("message", "Remboursement effectué", "txHash", mockTxHash));

        } catch (Exception e) {
            log.error("❌ Erreur remboursement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur remboursement: " + e.getMessage()));
        }
    }

    /**
     * ✅ ENDPOINT 7 : Annuler un paiement
     * POST /api/v1/payments/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(@RequestParam Integer reservationId) {
        log.info("📝 Annulation paiement pour réservation #{}", reservationId);

        try {
            BlockchainTransaction payment = transactionRepository
                    .findByReservationIdAndPaymentType(
                            reservationId,
                            BlockchainTransaction.PaymentType.BOOKING_PAYMENT
                    )
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

            if (payment.getPaymentStatus() == BlockchainTransaction.PaymentStatus.CONFIRMED) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Impossible d'annuler un paiement confirmé"));
            }

            payment.setPaymentStatus(BlockchainTransaction.PaymentStatus.FAILED);
            transactionRepository.save(payment);

            log.info("✅ Paiement annulé");
            return ResponseEntity.ok(Map.of("message", "Paiement annulé avec succès"));

        } catch (Exception e) {
            log.error("❌ Erreur annulation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur annulation: " + e.getMessage()));
        }
    }
}