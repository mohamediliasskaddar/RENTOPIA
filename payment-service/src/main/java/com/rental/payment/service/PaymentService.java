package com.rental.payment.service;

import com.rental.payment.client.BlockchainServiceClient;
import com.rental.payment.client.BookingServiceClient;
import com.rental.payment.dto.*;
import com.rental.payment.entity.BlockchainTransaction;
import com.rental.payment.exception.InsufficientBalanceException;
import com.rental.payment.exception.PaymentException;
import com.rental.payment.repository.BlockchainTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentService {  // ENLEVEZ @RequiredArgsConstructor

    private final BlockchainService blockchainService;
    private final BlockchainTransactionRepository transactionRepository;
    private final RabbitMQService rabbitMQService;  // Retirez @Lazy ici
    private final BlockchainServiceClient blockchainServiceClient;
    private final BookingServiceClient bookingServiceClient;
    private final ExternalServiceOrchestrator externalServiceOrchestrator; // AJOUTEZ si manquant

    // CONSTRUCTEUR PERSONNALISÉ avec @Lazy sur RabbitMQService
    @Autowired
    public PaymentService(
            BlockchainService blockchainService,
            BlockchainTransactionRepository transactionRepository,
            @Lazy RabbitMQService rabbitMQService,  // @Lazy SUR LE PARAMÈTRE
            BlockchainServiceClient blockchainServiceClient,
            BookingServiceClient bookingServiceClient,
            ExternalServiceOrchestrator externalServiceOrchestrator) {

        this.blockchainService = blockchainService;
        this.transactionRepository = transactionRepository;
        this.rabbitMQService = rabbitMQService;
        this.blockchainServiceClient = blockchainServiceClient;
        this.bookingServiceClient = bookingServiceClient;
        this.externalServiceOrchestrator = externalServiceOrchestrator;

        log.info("✅ PaymentService initialisé avec @Lazy sur RabbitMQService");
    }

    // ✅ INJECTION DE LA CLÉ PRIVÉE ADMIN DEPUIS application.yml
    @Value("${blockchain.wallet.private-key}")
    private String adminPrivateKey;

    /**
     * ✅ CORRIGÉ : Libération d'escrow avec clé admin
     */
    @Transactional
    public PaymentResponse releaseEscrow(Integer reservationId) {
        try {
            log.info("🔓 Libération d'escrow pour réservation #{}", reservationId);

            // 1. Récupérer les transactions existantes
            List<BlockchainTransaction> transactions = transactionRepository
                    .findByReservationId(reservationId);

            if (transactions.isEmpty()) {
                throw new PaymentException("Aucune transaction trouvée pour réservation #" + reservationId);
            }

            // 2. Vérifier que la réservation est en COMPLETED
            try {
                Map<String, Object> booking = bookingServiceClient.getBookingById(reservationId);
                if (booking != null) {
                    String status = (String) booking.get("status");
                    if (!"COMPLETED".equals(status)) {
                        throw new PaymentException(
                                "La réservation doit être COMPLETED pour libérer l'escrow. " +
                                        "Statut actuel: " + status + ". " +
                                        "Veuillez d'abord effectuer le check-out."
                        );
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Impossible de vérifier le statut booking: {}", e.getMessage());
            }

            // ✅ 3. Utiliser la clé admin depuis la config (PAS hardcodée)
            Map<String, String> checkoutRequest = new HashMap<>();
            checkoutRequest.put("userWalletPrivateKey", adminPrivateKey);

            log.info("🔑 Utilisation de la clé admin pour libération escrow");

            // 4. Appeler checkout() du blockchain service
            BlockchainTransactionResponse blockchainResponse =
                    blockchainServiceClient.releaseEscrow(reservationId.longValue(), checkoutRequest);

            // 5. Vérifier le succès
            if (blockchainResponse == null ||
                    blockchainResponse.getSuccess() == null ||
                    !blockchainResponse.getSuccess()) {

                String errorMsg = blockchainResponse != null ?
                        blockchainResponse.getError() : "Réponse nulle du blockchain service";
                throw new PaymentException("Échec libération escrow: " + errorMsg);
            }

            // 6. Récupérer le hash de transaction
            String txHash = blockchainResponse.getTransactionHash();
            if (txHash == null) {
                txHash = blockchainResponse.getTxHash();
            }

            log.info("✅ Transaction blockchain réussie: {}", txHash);

            // 7. Créer l'enregistrement en base
            BlockchainTransaction originalTransaction = transactions.get(0);
            BlockchainTransaction escrowTransaction = BlockchainTransaction.builder()
                    .reservationId(reservationId)
                    .payerWalletAddress(originalTransaction.getPayeeWalletAddress()) // Admin
                    .payeeWalletAddress(originalTransaction.getPayerWalletAddress()) // Host
                    .amountEth(originalTransaction.getAmountEth())
                    .transactionHash(txHash)
                    .paymentType(BlockchainTransaction.PaymentType.ESCROW_RELEASE)
                    .paymentStatus(BlockchainTransaction.PaymentStatus.CONFIRMED)
                    .gasFeeEth(blockchainResponse.getGasUsed() != null ? blockchainResponse.getGasUsed() : 0.0)
                    .confirmedAt(LocalDateTime.now())
                    .build();

            escrowTransaction = transactionRepository.save(escrowTransaction);

            // 8. Publier événement RabbitMQ
            rabbitMQService.publishEscrowReleased(reservationId, txHash);

            // 9. Retourner la réponse
            PaymentResponse response = PaymentResponse.fromEntity(escrowTransaction);
            response.setMessage("✅ Escrow libéré avec succès via checkout()");
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur libération escrow: {}", e.getMessage(), e);
            throw new PaymentException("Échec technique: " + e.getMessage());
        }
    }

    /**
     * ✅ NOUVEAU : Remboursement on-chain réel (pas de simulation)
     */
    @Transactional
    public PaymentResponse processRefund(Integer reservationId, Double amount, String reason) {
        try {
            log.info("💸 Remboursement pour réservation #{}, montant: {}, raison: {}",
                    reservationId, amount, reason);

            // 1. Récupérer les transactions existantes
            List<BlockchainTransaction> transactions = transactionRepository
                    .findByReservationId(reservationId);

            if (transactions.isEmpty()) {
                throw new PaymentException("Aucune transaction trouvée pour réservation #" + reservationId);
            }

            BlockchainTransaction originalTransaction = transactions.get(0);

            // 2. Déterminer le montant à rembourser
            Double refundAmount = amount != null ? amount : originalTransaction.getAmountEth();
            BigDecimal amountBD = BigDecimal.valueOf(refundAmount);

            // 3. Récupérer l'adresse du locataire (celui qui doit recevoir le remboursement)
            String tenantWallet = originalTransaction.getPayerWalletAddress();

            log.info("💰 Remboursement de {} ETH vers {}", refundAmount, tenantWallet);

            // ✅ 4. Effectuer le remboursement on-chain via BlockchainService
            String txHash = blockchainService.sendEther(tenantWallet, amountBD);

            log.info("✅ Transaction de remboursement envoyée: {}", txHash);

            // 5. Créer l'enregistrement en base
            BlockchainTransaction refundTransaction = BlockchainTransaction.builder()
                    .reservationId(reservationId)
                    .payerWalletAddress("ADMIN_WALLET") // L'admin rembourse
                    .payeeWalletAddress(tenantWallet) // Le locataire reçoit
                    .amountEth(refundAmount)
                    .transactionHash(txHash)
                    .paymentType(BlockchainTransaction.PaymentType.REFUND)
                    .paymentStatus(BlockchainTransaction.PaymentStatus.PROCESSING)
                    .build();

            refundTransaction = transactionRepository.save(refundTransaction);

            // 6. Marquer la transaction originale comme échouée
            originalTransaction.setPaymentStatus(BlockchainTransaction.PaymentStatus.FAILED);
            transactionRepository.save(originalTransaction);

            // 7. Publier événement RabbitMQ
            rabbitMQService.publishRefundProcessed(reservationId, txHash, refundAmount);

            // 8. Retourner la réponse
            PaymentResponse response = PaymentResponse.fromEntity(refundTransaction);
            response.setMessage("Remboursement traité avec succès. Transaction: " + txHash);
            return response;

        } catch (Exception e) {
            log.error("❌ Erreur remboursement: {}", e.getMessage(), e);
            throw new PaymentException("Échec remboursement: " + e.getMessage());
        }
    }

    /**
     * ✅ CORRIGÉ : Vérification réelle de transaction on-chain
     */
    @Transactional
    public PaymentResponse confirmTransaction(String txHash) {
        try {
            log.info("🔍 Confirmation de la transaction {}", txHash);

            // 1. Récupérer la transaction depuis la base
            BlockchainTransaction transaction = transactionRepository
                    .findByTransactionHash(txHash)
                    .orElseThrow(() -> new PaymentException("Transaction non trouvée: " + txHash));

            // ✅ 2. Vérifier le statut on-chain via BlockchainService
            boolean isConfirmed = blockchainService.isTransactionConfirmed(txHash);

            if (isConfirmed) {
                log.info("✅ Transaction confirmée on-chain: {}", txHash);

                // Mettre à jour le statut
                transaction.setPaymentStatus(BlockchainTransaction.PaymentStatus.CONFIRMED);
                transaction.setConfirmedAt(LocalDateTime.now());

                // Récupérer les frais de gas
                BigDecimal gasFee = blockchainService.getTransactionGasFee(txHash);
                transaction.setGasFeeEth(gasFee.doubleValue());

                transactionRepository.save(transaction);

                // Confirmer la réservation
                try {
                    bookingServiceClient.confirmBooking(
                            transaction.getReservationId(),
                            txHash
                    );
                } catch (Exception e) {
                    log.warn("⚠️ Échec mise à jour booking: {}", e.getMessage());
                }

                // Publier événement RabbitMQ
                rabbitMQService.sendToBookingService("PAYMENT_CONFIRMED",
                        Map.of(
                                "reservationId", transaction.getReservationId(),
                                "transactionHash", txHash,
                                "status", "CONFIRMED"
                        ));

                PaymentResponse response = PaymentResponse.fromEntity(transaction);
                response.setMessage("Paiement confirmé avec succès");
                return response;

            } else {
                log.info("⏳ Transaction en attente de confirmation: {}", txHash);

                PaymentResponse response = PaymentResponse.fromEntity(transaction);
                response.setMessage("Transaction en attente de confirmation on-chain");
                return response;
            }

        } catch (Exception e) {
            log.error("❌ Erreur confirmation: {}", e.getMessage(), e);
            throw new PaymentException("Erreur de confirmation: " + e.getMessage());
        }
    }

    // ========== AUTRES MÉTHODES (inchangées) ==========

    public BalanceResponse getWalletBalance(String walletAddress) {
        try {
            BigDecimal balance = blockchainService.getBalance(walletAddress);
            return BalanceResponse.builder()
                    .walletAddress(walletAddress)
                    .balanceEth(balance.doubleValue())
                    .message("Solde récupéré avec succès")
                    .build();
        } catch (Exception e) {
            log.error("❌ Erreur récupération solde: {}", e.getMessage());
            throw new PaymentException("Impossible de récupérer le solde: " + e.getMessage());
        }
    }

    public List<PaymentResponse> getReservationTransactions(Integer reservationId) {
        return transactionRepository.findByReservationId(reservationId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse createBookingPayment(PaymentRequest request) {
        try {
            log.info("💳 Vérification solde pour réservation #{}", request.getReservationId());

            // 1. Vérifier le solde du locataire
            BigDecimal tenantBalance = blockchainService.getBalance(request.getTenantWalletAddress());
            BigDecimal requiredAmount = BigDecimal.valueOf(request.getTotalAmountEth());

            log.info("💰 Solde locataire: {} ETH, Requis: {} ETH", tenantBalance, requiredAmount);

            if (tenantBalance.compareTo(requiredAmount) < 0) {
                throw new InsufficientBalanceException(
                        "Solde insuffisant. Requis: " + requiredAmount + " ETH, Disponible: " + tenantBalance + " ETH"
                );
            }

            // 2. Vérifier que l'utilisateur a un wallet
            if (request.getTenantWalletAddress() == null || request.getTenantWalletAddress().isEmpty()) {
                throw new PaymentException("L'utilisateur n'a pas de wallet configuré");
            }

            // 3. Retourner une réponse indiquant que l'utilisateur doit signer avec MetaMask
            log.info("📱 Demande de paiement préparée pour signature MetaMask");

            return PaymentResponse.builder()
                    .message("Demande de paiement créée. Veuillez signer avec MetaMask.")
                    .status("PENDING_SIGNATURE")
                    .build();

        } catch (InsufficientBalanceException | PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur création paiement: {}", e.getMessage(), e);
            throw new PaymentException("Échec du paiement: " + e.getMessage());
        }
    }
}