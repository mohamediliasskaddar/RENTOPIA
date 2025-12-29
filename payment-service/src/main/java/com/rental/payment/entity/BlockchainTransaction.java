package com.rental.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reservation_id", nullable = false)
    private Integer reservationId;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Column(name = "payer_wallet_address", nullable = false)
    private String payerWalletAddress;

    @Column(name = "payee_wallet_address", nullable = false)
    private String payeeWalletAddress;

    @Column(name = "amount_eth", nullable = false)
    private Double amountEth;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "gas_fee_eth")
    private Double gasFeeEth;

    @Column(name = "block_number")
    private Integer blockNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
    }

    public enum PaymentType {
        BOOKING_PAYMENT,
        ESCROW_RELEASE,
        PLATFORM_FEE,
        REFUND
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        CONFIRMED,
        FAILED
    }
}