package com.rental.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ========================================================================
// ✅ CreatePaymentDTO - Pour créer un paiement depuis Booking Service
// ========================================================================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentDTO {
    private Integer reservationId;
    private String payerWallet;      // Wallet du locataire
    private String payeeWallet;      // Wallet du propriétaire
    private Double amountEth;        // Montant en ETH
    private String paymentType;      // BOOKING_PAYMENT, REFUND, etc.
}
