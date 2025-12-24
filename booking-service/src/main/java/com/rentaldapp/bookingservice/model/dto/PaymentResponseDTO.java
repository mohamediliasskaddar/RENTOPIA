package com.rentaldapp.bookingservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour recevoir la réponse du Payment Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Integer id;
    private Integer reservationId;
    private String payerWalletAddress;
    private String payeeWalletAddress;
    private Double amountEth;
    private Double gasFeeEth;
    private String transactionHash;
    private Integer blockNumber;
    private String paymentType;
    private String paymentStatus;  // PENDING, PROCESSING, CONFIRMED, FAILED, REFUNDED
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}