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
public class BlockchainConfirmationEvent {

    private String transactionHash;
    private Integer blockNumber;
    private String status;
    private Double gasFee;
    private LocalDateTime confirmedAt;

    // Factory methods
    public static BlockchainConfirmationEvent confirmed(
            String txHash,
            Integer blockNumber,
            Double gasFee) {
        return BlockchainConfirmationEvent.builder()
                .transactionHash(txHash)
                .blockNumber(blockNumber)
                .status("CONFIRMED")
                .gasFee(gasFee)
                .confirmedAt(LocalDateTime.now())
                .build();
    }

    public static BlockchainConfirmationEvent failed(String txHash, String reason) {
        return BlockchainConfirmationEvent.builder()
                .transactionHash(txHash)
                .status("FAILED")
                .confirmedAt(LocalDateTime.now())
                .build();
    }
}