package com.rental.payment.dto;

import lombok.Data;

@Data
public class SignedTransactionRequest {
    private Integer reservationId;
    private String transactionHash; // Hash signé par MetaMask
    private String fromAddress;     // Adresse du locataire
    private Double amountEth;
    private Integer tenantId;       // ID du locataire
}