package com.rental.payment.controller;

import com.rental.payment.dto.BalanceResponse;
import com.rental.payment.dto.PaymentRequest;
import com.rental.payment.dto.PaymentResponse;
import com.rental.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /** POST /api/payments/create
* Créer un paiement de réservation
*/
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("📥 Demande de paiement reçue pour réservation #{}", request.getReservationId());
        PaymentResponse response = paymentService.createBookingPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/payments/confirm/{txHash}
     * Confirmer une transaction
     */
    @GetMapping("/confirm/{txHash}")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String txHash) {
        log.info("🔍 Confirmation de la transaction {}", txHash);
        PaymentResponse response = paymentService.confirmTransaction(txHash);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payments/balance/{walletAddress}
     * Vérifier le solde d'un wallet
     */
    @GetMapping("/balance/{walletAddress}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String walletAddress) {
        log.info("💰 Récupération du solde pour {}", walletAddress);
        BalanceResponse response = paymentService.getWalletBalance(walletAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payments/reservation/{reservationId}
     * Historique des paiements d'une réservation
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponse>> getReservationPayments(
            @PathVariable Integer reservationId) {

        log.info("📜 Récupération des paiements pour réservation #{}", reservationId);
        List<PaymentResponse> transactions = paymentService.getReservationTransactions(reservationId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /api/payments/health
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is running! 🚀");
    }
}