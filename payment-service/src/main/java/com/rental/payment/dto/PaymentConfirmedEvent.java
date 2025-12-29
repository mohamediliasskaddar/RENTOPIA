package com.rental.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {

    private Integer reservationId;
    private Integer transactionId;
    private String transactionHash;
    private Double amountEth;
    private String tenantWallet;
    private String hostWallet;
    private String status;
    private Integer blockNumber;
    private LocalDateTime confirmedAt;

    // Factory method
    public static PaymentConfirmedEvent from(
            Integer reservationId,
            Integer transactionId,
            String txHash,
            Double amount,
            String tenantWallet,
            String hostWallet,
            Integer blockNumber) {
        return PaymentConfirmedEvent.builder()
                .reservationId(reservationId)
                .transactionId(transactionId)
                .transactionHash(txHash)
                .amountEth(amount)
                .tenantWallet(tenantWallet)
                .hostWallet(hostWallet)
                .status("CONFIRMED")
                .blockNumber(blockNumber)
                .confirmedAt(LocalDateTime.now())
                .build();
    }
}