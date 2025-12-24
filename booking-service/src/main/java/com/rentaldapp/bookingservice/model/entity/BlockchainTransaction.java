package com.rentaldapp.bookingservice.model.entity;

import com.rentaldapp.bookingservice.model.enums.PaymentStatus;
import com.rentaldapp.bookingservice.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reservation_id", nullable = false)
    private Integer reservationId;

    @Column(name = "payer_wallet_address", nullable = false, length = 255)
    private String payerWalletAddress;

    @Column(name = "payee_wallet_address", nullable = false, length = 255)
    private String payeeWalletAddress;

    @Column(name = "amount_eth", nullable = false, columnDefinition = "DOUBLE")
    private Double amountEth;

    @Column(name = "gas_fee_eth", columnDefinition = "DOUBLE")
    private Double gasFeeEth;

    @Column(name = "transaction_hash", nullable = false, unique = true, length = 255)
    private String transactionHash;

    @Column(name = "block_number")
    private Integer blockNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 50)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
}
