package com.rental.payment.dto;

import com.rental.payment.entity.BlockchainTransaction;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Integer transactionId;
    private String transactionHash;
    private String status;
    private Double amountEth;
    private Double gasFeeEth;
    private Integer blockNumber;
    private LocalDateTime createdAt;
    private String message;
    private String explorerUrl;  // Lien Etherscan

    public static PaymentResponse fromEntity(BlockchainTransaction transaction) {
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .transactionHash(transaction.getTransactionHash())
                .status(transaction.getPaymentStatus().name())
                .amountEth(transaction.getAmountEth())
                .gasFeeEth(transaction.getGasFeeEth())
                .blockNumber(transaction.getBlockNumber())
                .createdAt(transaction.getCreatedAt())
                .explorerUrl("https://sepolia.etherscan.io/tx/" + transaction.getTransactionHash())
                .build();
    }
}