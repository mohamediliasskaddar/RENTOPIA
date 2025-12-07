package com.rental.payment.service;

import com.rental.payment.dto.BalanceResponse;
import com.rental.payment.dto.PaymentRequest;
import com.rental.payment.dto.PaymentResponse;
import com.rental.payment.entity.BlockchainTransaction;
import com.rental.payment.exception.InsufficientBalanceException;
import com.rental.payment.exception.PaymentException;
import com.rental.payment.repository.BlockchainTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BlockchainService blockchainService;
    private final BlockchainTransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Créer un paiement de réservation avec escrow
     */
    @Transactional
    public PaymentResponse createBookingPayment(PaymentRequest request) {
        try {
            log.info("💳 Création d'un paiement pour la réservation #{}", request.getReservationId());

            // 1. Vérifier le solde du locataire
            BigDecimal tenantBalance = blockchainService.getBalance(request.getTenantWalletAddress());
            BigDecimal requiredAmount = BigDecimal.valueOf(request.getTotalAmountEth());

            log.info("💰 Solde locataire: {} ETH, Requis: {} ETH", tenantBalance, requiredAmount);

            if (tenantBalance.compareTo(requiredAmount) < 0) {
                throw new InsufficientBalanceException(
                        "Solde insuffisant. Requis: " + requiredAmount + " ETH, Disponible: " + tenantBalance + " ETH"
                );
            }

            // 2. Vérifier le solde du wallet admin (pour payer les gas fees)
            String adminWallet = blockchainService.getCredentials().getAddress();
            BigDecimal adminBalance = blockchainService.getBalance(adminWallet);
            log.info("💰 Solde wallet admin (gas): {} ETH", adminBalance);

            if (adminBalance.compareTo(new BigDecimal("0.01")) < 0) {
                throw new PaymentException(
                        "Le wallet admin n'a pas assez d'ETH pour payer les frais de gas. " +
                                "Solde actuel: " + adminBalance + " ETH. " +
                                "Veuillez recharger le wallet: " + adminWallet
                );
            }

            // 3. Envoyer la transaction blockchain
            log.info("📤 Envoi de la transaction blockchain...");
            String txHash;
            try {
                txHash = blockchainService.sendEther(
                        request.getHostWalletAddress(),
                        BigDecimal.valueOf(request.getAmountEth())
                );
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de la transaction: {}", e.getMessage(), e);
                throw new PaymentException("Impossible d'envoyer la transaction blockchain: " + e.getMessage());
            }

            // 4. Vérifier que le hash n'est pas null
            if (txHash == null || txHash.isEmpty()) {
                throw new PaymentException("La transaction blockchain n'a pas retourné de hash. Vérifiez la configuration.");
            }

            log.info("✅ Transaction envoyée! Hash: {}", txHash);

            // 5. Créer l'enregistrement de transaction
            BlockchainTransaction transaction = BlockchainTransaction.builder()
                    .reservationId(request.getReservationId())
                    .payerWalletAddress(request.getTenantWalletAddress())
                    .payeeWalletAddress(request.getHostWalletAddress())
                    .amountEth(request.getAmountEth())
                    .transactionHash(txHash)
                    .paymentType(BlockchainTransaction.PaymentType.BOOKING_PAYMENT)
                    .paymentStatus(BlockchainTransaction.PaymentStatus.PROCESSING)
                    .build();

            transaction = transactionRepository.save(transaction);

            log.info("✅ Paiement créé avec succès! ID: {}", transaction.getId());

            // 6. Publier événement RabbitMQ
            publishPaymentEvent(transaction, "payment.created");

            // 7. Créer la réponse
            PaymentResponse response = PaymentResponse.fromEntity(transaction);
            response.setMessage("Paiement en cours de traitement");
            return response;

        } catch (InsufficientBalanceException e) {
            log.error("❌ Solde insuffisant: {}", e.getMessage());
            throw e;
        } catch (PaymentException e) {
            log.error("❌ Erreur de paiement: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du paiement: {}", e.getMessage(), e);
            throw new PaymentException("Échec du paiement: " + e.getMessage());
        }
    }
    /**
     * Vérifier et confirmer une transaction
     */
    @Transactional
    public PaymentResponse confirmTransaction(String txHash) {
        try {
            log.info("🔍 Vérification de la transaction {}", txHash);

            BlockchainTransaction transaction = transactionRepository
                    .findByTransactionHash(txHash)
                    .orElseThrow(() -> new PaymentException("Transaction non trouvée"));

            // Vérifier le statut sur la blockchain
            TransactionReceipt receipt = blockchainService.getTransactionReceipt(txHash);

            if (receipt != null && receipt.isStatusOK()) {
                // Transaction confirmée
                transaction.setPaymentStatus(BlockchainTransaction.PaymentStatus.CONFIRMED);
                transaction.setConfirmedAt(LocalDateTime.now());
                transaction.setBlockNumber(receipt.getBlockNumber().intValue());

                // Calculer les frais de gas
                BigDecimal gasFee = blockchainService.getTransactionGasFee(txHash);
                transaction.setGasFeeEth(gasFee.doubleValue());

                transactionRepository.save(transaction);

                log.info("✅ Transaction confirmée au bloc #{}", receipt.getBlockNumber());

                // Publier événement
                publishPaymentEvent(transaction, "payment.confirmed");

                PaymentResponse response = PaymentResponse.fromEntity(transaction);
                response.setMessage("Paiement confirmé avec succès");
                return response;

            } else if (receipt != null && !receipt.isStatusOK()) {
                // Transaction échouée
                transaction.setPaymentStatus(BlockchainTransaction.PaymentStatus.FAILED);
                transactionRepository.save(transaction);

                publishPaymentEvent(transaction, "payment.failed");

                throw new PaymentException("Transaction échouée sur la blockchain");
            } else {
                // Transaction en attente
                PaymentResponse response = PaymentResponse.fromEntity(transaction);
                response.setMessage("Transaction en attente de confirmation");
                return response;
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la confirmation: {}", e.getMessage());
            throw new PaymentException("Erreur de confirmation: " + e.getMessage());
        }
    }

    /**
     * Obtenir le solde d'un wallet
     */
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
            throw new PaymentException("Impossible de récupérer le solde");
        }
    }

    /**
     * Obtenir l'historique des transactions d'une réservation
     */
    public List<PaymentResponse> getReservationTransactions(Integer reservationId) {
        return transactionRepository.findByReservationId(reservationId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Publier un événement de paiement via RabbitMQ
     */
    private void publishPaymentEvent(BlockchainTransaction transaction, String eventType) {
        try {
            rabbitTemplate.convertAndSend(
                    "rental.exchange",
                    "payment." + eventType,
                    transaction
            );
            log.info("📨 Événement publié: {}", eventType);
        } catch (Exception e) {
            log.error("❌ Erreur publication événement: {}", e.getMessage());
        }
    }
}