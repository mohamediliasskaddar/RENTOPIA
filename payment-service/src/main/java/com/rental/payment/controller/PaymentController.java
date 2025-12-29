package com.rental.payment.controller;

import com.rental.payment.client.BookingServiceClient;
import com.rental.payment.dto.*;
import com.rental.payment.service.BlockchainService;
import com.rental.payment.service.ExternalServiceOrchestrator;
import com.rental.payment.service.PaymentService;
import com.rental.payment.service.RabbitMQService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rental.payment.repository.BlockchainTransactionRepository; // AJOUTEZ CE IMPORT
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.rental.payment.entity.BlockchainTransaction;
import com.rental.payment.entity.BlockchainTransaction.PaymentType;
import com.rental.payment.entity.BlockchainTransaction.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {  // ENLEVEZ @RequiredArgsConstructor

    private final PaymentService paymentService;
    private final ExternalServiceOrchestrator externalServiceOrchestrator;
    private final BlockchainService blockchainService;
    private final BookingServiceClient bookingServiceClient;
    private final RabbitMQService rabbitMQService;
    private final BlockchainTransactionRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    // CONSTRUCTEUR PERSONNALISÉ avec @Lazy
    @Autowired
    public PaymentController(
            @Lazy PaymentService paymentService,
            ExternalServiceOrchestrator externalServiceOrchestrator,
            BlockchainService blockchainService,
            BookingServiceClient bookingServiceClient,
            @Lazy RabbitMQService rabbitMQService,
            BlockchainTransactionRepository paymentRepository,
            RabbitTemplate rabbitTemplate) {

        this.paymentService = paymentService;
        this.externalServiceOrchestrator = externalServiceOrchestrator;
        this.blockchainService = blockchainService;
        this.bookingServiceClient = bookingServiceClient;
        this.rabbitMQService = rabbitMQService;
        this.paymentRepository = paymentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }


    // ============ ENDPOINTS DE PAIEMENT ============

    /**
     * POST /api/payments/create
     * Créer un paiement de réservation
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("📥 Demande de paiement reçue pour réservation #{}", request.getReservationId());

        try {
            PaymentResponse response = paymentService.createBookingPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("❌ Erreur création paiement: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failed(e.getMessage()));
        }
    }

    /**
     * POST /api/payments/escrow/release/{reservationId}
     * Libérer l'escrow
     */
    @PostMapping("/escrow/release/{reservationId}")
    public ResponseEntity<PaymentResponse> releaseEscrow(
            @PathVariable Integer reservationId) {
        log.info("🔓 Demande de libération d'escrow pour réservation #{}", reservationId);

        try {
            PaymentResponse response = paymentService.releaseEscrow(reservationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur libération escrow: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failed(e.getMessage()));
        }
    }

    /**
     * POST /api/payments/refund/{reservationId}
     * Effectuer un remboursement
     */
    @PostMapping("/refund/{reservationId}")
    public ResponseEntity<PaymentResponse> processRefund(
            @PathVariable Integer reservationId,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) String reason) {
        log.info("💸 Demande de remboursement pour réservation #{}", reservationId);

        try {
            PaymentResponse response = paymentService.processRefund(reservationId, amount, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur remboursement: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failed(e.getMessage()));
        }
    }


    // ============ NOUVEL ENDPOINT WEB3 ============

    /**
     * POST /api/payments/confirm-payment
     * Confirmer un paiement signé par MetaMask (modèle Web3)
     */
    @PostMapping("/confirm-payment")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Valid @RequestBody SignedTransactionRequest request) {

        log.info("✅ Réception transaction signée pour réservation #{} - Hash: {}",
                request.getReservationId(), request.getTransactionHash());

        try {
            // 1. Créer l'enregistrement en base avec le hash reçu
            BlockchainTransaction transaction = BlockchainTransaction.builder()
                    .reservationId(request.getReservationId())
                    .payerWalletAddress(request.getFromAddress())
                    .payeeWalletAddress("")
                    .amountEth(request.getAmountEth())
                    .transactionHash(request.getTransactionHash())
                    .paymentType(PaymentType.BOOKING_PAYMENT)
                    .paymentStatus(PaymentStatus.PROCESSING)
                    .build();

            transaction = paymentRepository.save(transaction);
            log.info("✅ Transaction enregistrée: ID {}", transaction.getId());

            // 2. Récupérer l'adresse du host depuis booking service
            String hostWalletAddress = "0x1234567890123456789012345678901234567890";
            try {
                Map<String, Object> booking = bookingServiceClient.getBookingById(request.getReservationId());
                if (booking != null && booking.containsKey("hostWalletAddress")) {
                    hostWalletAddress = (String) booking.get("hostWalletAddress");
                }
            } catch (Exception e) {
                log.warn("⚠️ Impossible de récupérer hostWalletAddress: {}", e.getMessage());
            }

            transaction.setPayeeWalletAddress(hostWalletAddress);
            paymentRepository.save(transaction);

            // 3. Mettre à jour la réservation
            try {
                bookingServiceClient.confirmBooking(
                        request.getReservationId(),
                        request.getTransactionHash()
                );
            } catch (Exception e) {
                log.warn("⚠️ Échec mise à jour booking: {}", e.getMessage());
            }

            // 4. Envoyer notification PAYMENT_RECEIVED (UNIQUEMENT)
            try {
                log.info("🔔 Création notification PAYMENT_RECEIVED pour réservation #{}",
                        request.getReservationId());

                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .userId(request.getTenantId())
                        .notificationType("PAYMENT_RECEIVED")  // CORRECT - existe dans l'enum
                        .title("Paiement reçu")
                        .message("Votre paiement de " + request.getAmountEth() + " ETH a été reçu et est en traitement")
                        .reservationId(request.getReservationId())
                        .data(Map.of(
                                "transactionHash", request.getTransactionHash(),
                                "amount", request.getAmountEth(),
                                "reservationId", request.getReservationId(),
                                "status", "RECEIVED"
                        ))
                        .build();

                log.info("📤 Envoi notification: {}", notificationRequest);

                // CORRECTION : Pas besoin d'attribuer à une variable, la méthode retourne void
                externalServiceOrchestrator.sendNotification(notificationRequest);
                log.info("✅ Notification envoyée avec succès");

            } catch (Exception e) {
                log.error("❌ ERREUR notification PAYMENT_RECEIVED: {}", e.getMessage(), e);
            }

            // 5. Publier événement RabbitMQ
            try {
                rabbitMQService.sendToBookingService("PAYMENT_CREATED",
                        Map.of(
                                "reservationId", request.getReservationId(),
                                "transactionHash", request.getTransactionHash(),
                                "status", "PROCESSING",
                                "paymentId", transaction.getId()
                        ));
            } catch (Exception e) {
                log.warn("⚠️ Échec publication RabbitMQ: {}", e.getMessage());
            }

            // 6. Retourner la réponse
            PaymentResponse response = PaymentResponse.fromEntity(transaction);
            response.setMessage("Paiement confirmé avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur confirmation paiement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failed(e.getMessage()));
        }
    }





    // ============ ENDPOINTS DE VÉRIFICATION ============

    /**
     * GET /api/payments/confirm/{txHash}
     * Confirmer une transaction
     */
    @GetMapping("/confirm/{txHash}")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String txHash) {
        log.info("🔍 Confirmation de la transaction {}", txHash);

        try {
            PaymentResponse response = paymentService.confirmTransaction(txHash);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur confirmation: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(PaymentResponse.failed("Transaction non trouvée"));
        }
    }

    /**
     * GET /api/payments/status/{reservationId}
     * Statut des paiements d'une réservation
     */
    @GetMapping("/status/{reservationId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(
            @PathVariable Integer reservationId) {
        log.info("📊 Récupération du statut pour réservation #{}", reservationId);

        List<PaymentResponse> transactions = paymentService.getReservationTransactions(reservationId);

        boolean hasConfirmedPayment = transactions.stream()
                .anyMatch(tx -> "CONFIRMED".equals(tx.getStatus()));

        double totalPaid = transactions.stream()
                .filter(tx -> "CONFIRMED".equals(tx.getStatus()))
                .mapToDouble(PaymentResponse::getAmountEth)
                .sum();

        Map<String, Object> status = Map.of(
                "reservationId", reservationId,
                "hasConfirmedPayment", hasConfirmedPayment,
                "totalPaid", totalPaid,
                "transactionCount", transactions.size(),
                "transactions", transactions,
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/payments/balance/{walletAddress}
     * Vérifier le solde d'un wallet
     */
    @GetMapping("/balance/{walletAddress}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String walletAddress) {
        log.info("💰 Récupération du solde pour {}", walletAddress);

        try {
            BalanceResponse response = paymentService.getWalletBalance(walletAddress);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur récupération solde: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BalanceResponse.builder()
                            .walletAddress(walletAddress)
                            .message("Erreur lors de la récupération du solde")
                            .build());
        }
    }

    /**
     * GET /api/payments/reservation/{reservationId}
     * Historique des paiements
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponse>> getReservationPayments(
            @PathVariable Integer reservationId) {
        log.info("📜 Récupération des paiements pour réservation #{}", reservationId);

        List<PaymentResponse> transactions = paymentService.getReservationTransactions(reservationId);
        return ResponseEntity.ok(transactions);
    }

    // ============ ENDPOINTS DE MONITORING ============

    /**
     * GET /api/payments/health
     * Health check complet du service avec vérification des dépendances
     */


    // Méthode pour vérifier le service blockchain
    private boolean checkBlockchainServiceHealth() {
        try {
            // Appeler votre méthode existante
            ResponseEntity<Map<String, Object>> response = checkBlockchainHealth();

            // Vérifier si la réponse est OK et contient le statut "UP"
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> blockchainHealth = response.getBody();

                // Plusieurs façons de vérifier selon la structure de votre réponse
                // 1. Si votre réponse a un champ "status"
                if (blockchainHealth.containsKey("status")) {
                    return "UP".equals(blockchainHealth.get("status"));
                }
                // 2. Si votre réponse a un champ "blockchainService"
                else if (blockchainHealth.containsKey("blockchainService")) {
                    return "UP".equals(blockchainHealth.get("blockchainService"));
                }
                // 3. Par défaut, si la réponse 2xx, on considère que c'est OK
                else {
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            log.error("❌ Échec de vérification du service blockchain: {}", e.getMessage());
            return false;
        }
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("🩺 Health check complet du service payment");

        try {
            // Vérifier la base de données
            boolean dbHealthy = checkDatabaseHealth();

            // Vérifier le service blockchain
            boolean blockchainHealthy = checkBlockchainServiceHealth();

            // Vérifier RabbitMQ
            boolean rabbitmqHealthy = checkRabbitMQHealth();

            // Vérifier le service booking
            Map<String, Object> bookingHealth = checkBookingServiceHealth();
            boolean bookingHealthy = "UP".equals(bookingHealth.get("status"));

            // Déterminer le statut global
            boolean overallStatus = dbHealthy && blockchainHealthy && bookingHealthy;

            // Construire la réponse
            Map<String, Object> health = Map.of(
                    "status", overallStatus ? "UP" : "DOWN",
                    "service", "payment-service",
                    "port", 8084,
                    "timestamp", LocalDateTime.now(),
                    "dependencies", Map.of(
                            "database", Map.of("status", dbHealthy ? "CONNECTED" : "DISCONNECTED"),
                            "blockchain", Map.of(
                                    "network", "sepolia",
                                    "status", blockchainHealthy ? "CONNECTED" : "DISCONNECTED"
                            ),
                            "rabbitmq", Map.of("status", rabbitmqHealthy ? "CONNECTED" : "DISCONNECTED"),
                            "booking-service", bookingHealth
                    ),
                    "details", Map.of(
                            "database", getDatabaseHealthDetails(dbHealthy),
                            "blockchain", getBlockchainHealthDetails(blockchainHealthy),
                            "rabbitmq", getRabbitMQHealthDetails(rabbitmqHealthy),
                            "bookingService", bookingHealth
                    )
            );

            // Retourner le statut HTTP approprié
            HttpStatus status = overallStatus ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(health);

        } catch (Exception e) {
            log.error("❌ Erreur critique lors du health check: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "DOWN",
                            "error", "Health check failed: " + e.getMessage(),
                            "service", "payment-service",
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    // ============ MÉTHODES DE VÉRIFICATION ============

    private boolean checkDatabaseHealth() {
        try {
            log.debug("🔍 Vérification de la connexion à la base de données...");

            // Méthode 1: Faire une simple requête COUNT
            // Cela vérifie que la connexion à la base de données fonctionne
            long count = paymentRepository.count();
            log.debug("✅ Base de données connectée. Nombre d'enregistrements dans payments: {}", count);

            return true;

        } catch (Exception e) {
            log.error("❌ Échec de vérification de la base de données: {}", e.getMessage());
            return false;
        }
    }



    private boolean checkRabbitMQHealth() {
        try {
            log.debug("🔍 Vérification de la connexion à RabbitMQ...");

            // Méthode 1: Vérifier si on peut déclarer une file passivement
            // Cela vérifie que la connexion RabbitMQ est active
            rabbitTemplate.execute(channel -> {
                // Vérifier une file qui existe (ou tenter d'en créer une temporaire)
                try {
                    // Si vous avez une file existante nommée "payment.queue"
                    channel.queueDeclarePassive("payment.queue");
                } catch (Exception e) {
                    // Si la file n'existe pas, on peut en créer une temporaire
                    // ou simplement vérifier la connexion avec une opération simple
                    channel.queueDeclare("health-check-temp", false, false, true, null);
                    channel.queueDelete("health-check-temp");
                }
                return null;
            });

            log.debug("✅ RabbitMQ connecté avec succès");
            return true;

        } catch (Exception e) {
            log.error("❌ Échec de vérification de RabbitMQ: {}", e.getMessage());
            return false;
        }
    }

    private Map<String, Object> checkBookingServiceHealth() {
        try {
            log.debug("🔍 Vérification de la connexion au booking-service...");
            Map<String, Object> bookingResponse = bookingServiceClient.healthCheck();

            if (bookingResponse != null && !bookingResponse.isEmpty()) {
                // Extraire le statut du booking service
                String bookingStatus = (String) bookingResponse.get("status");
                boolean isUp = "UP".equals(bookingStatus) || "FALLBACK".equals(bookingStatus);

                return Map.of(
                        "status", isUp ? "UP" : "DOWN",
                        "service", bookingResponse.getOrDefault("service", "booking-service"),
                        "bookingResponse", bookingResponse,
                        "responseTime", LocalDateTime.now(),
                        "message", isUp ?
                                "Booking service is reachable and healthy" :
                                "Booking service responded but status is DOWN",
                        "isFallback", "FALLBACK".equals(bookingStatus) // Indique si on utilise le fallback
                );
            } else {
                return Map.of(
                        "status", "DOWN",
                        "error", "Booking service returned empty or null response",
                        "message", "Cannot connect to booking service",
                        "timestamp", LocalDateTime.now()
                );
            }
        } catch (Exception e) {
            log.error("❌ Échec de connexion au booking service: {}", e.getMessage());
            return Map.of(
                    "status", "DOWN",
                    "error", e.getMessage(),
                    "message", "Booking service is unreachable",
                    "timestamp", LocalDateTime.now()
            );
        }
    }

    // ============ MÉTHODES DÉTAILS ============

    private Map<String, Object> getDatabaseHealthDetails(boolean isHealthy) {
        return Map.of(
                "status", isHealthy ? "CONNECTED" : "DISCONNECTED",
                "message", isHealthy ? "Database connection successful" : "Database connection failed",
                "timestamp", LocalDateTime.now()
        );
    }

    private Map<String, Object> getBlockchainHealthDetails(boolean isHealthy) {
        return Map.of(
                "status", isHealthy ? "CONNECTED" : "DISCONNECTED",
                "network", "sepolia",
                "message", isHealthy ? "Blockchain service is connected" : "Blockchain service is disconnected",
                "timestamp", LocalDateTime.now()
        );
    }

    private Map<String, Object> getRabbitMQHealthDetails(boolean isHealthy) {
        return Map.of(
                "status", isHealthy ? "CONNECTED" : "DISCONNECTED",
                "message", isHealthy ? "RabbitMQ is connected" : "RabbitMQ connection failed",
                "timestamp", LocalDateTime.now()
        );
    }


// ============ ENDPOINTS BLOCKCHAIN ============

    /**
     * POST /api/payments/blockchain/balance/verify
     * Vérifier si un wallet a assez de solde avant paiement
     */
    @PostMapping("/blockchain/balance/verify")
    public ResponseEntity<Map<String, Object>> verifyBalanceForPayment(
            @Valid @RequestBody BalanceVerificationRequest request) {
        log.info("💰 Vérification solde pour wallet: {}", request.getWalletAddress());

        try {
            // 1. Vérifier solde via blockchain service local
            BigDecimal balance = blockchainService.getBalance(request.getWalletAddress());
            BigDecimal required = BigDecimal.valueOf(request.getRequiredAmountEth());

            boolean hasEnoughBalance = balance.compareTo(required) >= 0;

            Map<String, Object> response = Map.of(
                    "walletAddress", request.getWalletAddress(),
                    "currentBalance", balance.doubleValue(),
                    "requiredAmount", request.getRequiredAmountEth(),
                    "hasSufficientBalance", hasEnoughBalance,
                    "message", hasEnoughBalance ?
                            "Solde suffisant" : "Solde insuffisant"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur vérification solde: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/payments/blockchain/wallet/create
     * Créer un wallet pour un utilisateur (au moment du premier paiement)
     */
    @PostMapping("/blockchain/wallet/create")
    public ResponseEntity<Map<String, Object>> createUserWallet(
            @RequestParam Integer userId) {
        log.info("🔐 Création wallet pour utilisateur #{}", userId);

        try {
            Map<String, Object> wallet = externalServiceOrchestrator.createWallet();

            if (wallet != null) {
                return ResponseEntity.ok(Map.of(
                        "userId", userId,
                        "wallet", wallet,
                        "message", "Wallet créé avec succès",
                        "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "error", "Service blockchain indisponible",
                                "userId", userId,
                                "timestamp", LocalDateTime.now()
                        ));
            }

        } catch (Exception e) {
            log.error("❌ Erreur création wallet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "userId", userId,
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * GET /api/payments/blockchain/transaction/{txHash}/status
     * Vérifier le statut d'une transaction blockchain
     */
    @GetMapping("/blockchain/transaction/{txHash}/status")
    public ResponseEntity<Map<String, Object>> getTransactionStatus(
            @PathVariable String txHash) {
        log.info("🔍 Vérification statut transaction: {}", txHash);

        try {
            boolean isConfirmed = blockchainService.isTransactionConfirmed(txHash);
            BigDecimal gasFee = blockchainService.getTransactionGasFee(txHash);

            Map<String, Object> response = Map.of(
                    "transactionHash", txHash,
                    "status", isConfirmed ? "CONFIRMED" : "PENDING",
                    "gasFeeEth", gasFee.doubleValue(),
                    "message", isConfirmed ?
                            "Transaction confirmée" : "Transaction en attente",
                    "timestamp", LocalDateTime.now()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur vérification transaction: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "transactionHash", txHash,
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * GET /api/payments/blockchain/health
     * Vérifier la santé du service blockchain
     */
    @GetMapping("/blockchain/health")
    public ResponseEntity<Map<String, Object>> checkBlockchainHealth() {
        log.info("🩺 Vérification santé blockchain");

        try {
            Map<String, Object> healthResponse = externalServiceOrchestrator.checkBlockchainHealth();

            if (healthResponse != null) {
                return ResponseEntity.ok(healthResponse);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "error", "Service blockchain indisponible",
                                "blockchainService", "DOWN",
                                "timestamp", LocalDateTime.now()
                        ));
            }

        } catch (Exception e) {
            log.error("❌ Erreur vérification santé blockchain: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "blockchainService", "DOWN",
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    // ============ ENDPOINTS NOTIFICATION ============

    /**
     * POST /api/payments/notifications/send
     * Envoyer une notification de paiement (utilisé en interne par RabbitMQ)
     */
    @PostMapping("/notifications/send")
    public ResponseEntity<Map<String, Object>> sendPaymentNotification(
            @Valid @RequestBody NotificationRequest request) {
        log.info("📧 Envoi notification de paiement pour utilisateur #{}",
                request.getUserId());

        try {
            externalServiceOrchestrator.sendNotification(request);

            return ResponseEntity.ok(Map.of(
                    "userId", request.getUserId(),
                    "type", request.getNotificationType(),
                    "message", "Notification envoyée",
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("❌ Erreur envoi notification: {}", e.getMessage());
            return ResponseEntity.ok(Map.of( // On retourne OK même en cas d'erreur (non critique)
                    "userId", request.getUserId(),
                    "type", request.getNotificationType(),
                    "message", "Notification non envoyée (service temporairement indisponible)",
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * GET /api/payments/{reservationId}/notifications
     * Récupérer les notifications liées à un paiement
     */
    @GetMapping("/{reservationId}/notifications")
    public ResponseEntity<Map<String, Object>> getPaymentNotifications(
            @PathVariable Integer reservationId) {
        log.info("📜 Récupération notifications pour réservation #{}", reservationId);

        try {
            // 1. Récupérer les transactions de la réservation
            List<PaymentResponse> transactions = paymentService.getReservationTransactions(reservationId);

            log.info("📊 Transactions trouvées pour réservation #{}: {}", reservationId, transactions.size());

            Integer userId = getUserIdFromReservation(reservationId);
            log.info("📊 UserId récupéré: {}", userId);

            log.info("📊 Utilisation de userId: {}", userId);

            // 3. Récupérer les notifications de l'utilisateur
            List<Map<String, Object>> allNotifications = externalServiceOrchestrator.getUserNotifications(userId);

            log.info("📊 Notifications récupérées (total): {}", allNotifications.size());

            // DEBUG: Afficher les types de notification
            for (Map<String, Object> notif : allNotifications) {
                log.info("📊 Notification - notificationType: {}, reservationId: {}",
                        notif.get("notificationType"), notif.get("reservationId"));
            }

            // 4. Filtrer pour garder seulement celles liées à CETTE réservation
            List<Map<String, Object>> reservationNotifications = allNotifications.stream()
                    .filter(notification -> {
                        Object notifReservationId = notification.get("reservationId");
                        // Vérifier si c'est la même réservation
                        return notifReservationId != null &&
                                notifReservationId.toString().equals(reservationId.toString());
                    })
                    .collect(Collectors.toList());

            log.info("📊 Notifications pour réservation #{}: {}", reservationId, reservationNotifications.size());

            // 5. Optionnel: Filtrer par type (CORRIGEZ le champ)
            List<Map<String, Object>> paymentNotifications = reservationNotifications.stream()
                    .filter(notification -> {
                        String type = (String) notification.get("notificationType"); // CORRIGÉ: notificationType, pas type
                        return type != null && (
                                type.contains("PAYMENT") ||
                                        type.contains("BOOKING") || // Ajoutez d'autres types si besoin
                                        type.contains("CHECK_IN") ||
                                        type.contains("MESSAGE")
                        );
                    })
                    .collect(Collectors.toList());

            log.info("📊 Notifications filtrées par type: {}", paymentNotifications.size());

            return ResponseEntity.ok(Map.of(
                    "reservationId", reservationId,
                    "userId", userId,
                    "notifications", reservationNotifications, // ou paymentNotifications
                    "allNotificationsCount", allNotifications.size(),
                    "reservationNotificationsCount", reservationNotifications.size(),
                    "filteredNotificationsCount", paymentNotifications.size(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("❌ Erreur récupération notifications: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "reservationId", reservationId,
                    "notifications", List.of(),
                    "message", "Erreur lors de la récupération: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
    /**
     * GET /api/payments/notifications/health
     * Vérifier la santé du service notification
     */
    @GetMapping("/notifications/health")
    public ResponseEntity<Map<String, Object>> checkNotificationHealth() {
        log.info("🩺 Vérification santé notification service");

        try {
            Map<String, Object> healthResponse = externalServiceOrchestrator.checkNotificationServiceHealth();

            // ✅ Retourne directement la réponse du service notification
            return ResponseEntity.ok(healthResponse);

        } catch (Exception e) {
            log.error("❌ Erreur vérification santé notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "notificationService", "DOWN",
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }



    private Integer getUserIdFromReservation(Integer reservationId) {
        try {
            // 1. Appeler le Booking Service pour récupérer les détails de la réservation
            Map<String, Object> booking = bookingServiceClient.getBookingById(reservationId);  // ❌ Plus de ResponseEntity

            if (booking != null && !booking.isEmpty()) {
                // 2. Extraire l'ID utilisateur
                // Le booking service retourne probablement "userId" ou "tenantId"
                Integer userId = null;

                // Essayer différents champs possibles
                if (booking.containsKey("tenantId")) {
                    userId = (Integer) booking.get("tenantId");
                } else if (booking.containsKey("userId")) {
                    userId = (Integer) booking.get("userId");
                } else if (booking.containsKey("user") && booking.get("user") instanceof Map) {
                    // Si l'utilisateur est un objet imbriqué
                    Map<String, Object> user = (Map<String, Object>) booking.get("user");
                    if (user.containsKey("id")) {
                        userId = (Integer) user.get("id");
                    }
                }

                if (userId != null) {
                    log.debug("✅ UserId {} trouvé pour réservation #{}", userId, reservationId);
                    return userId;
                } else {
                    log.warn("⚠️ Aucun userId/tenantId trouvé dans la réservation #{}: {}",
                            reservationId, booking.keySet());
                    return null;
                }
            } else {
                log.warn("⚠️ Réservation #{} non trouvée dans booking service", reservationId);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération de la réservation #{}: {}",
                    reservationId, e.getMessage());
            return null;
        }
    }


}