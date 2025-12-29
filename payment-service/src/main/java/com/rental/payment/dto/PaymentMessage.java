package com.rental.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage {

    private PaymentMessageType type; // ✅ UTILISER UN ENUM au lieu de String
    private Integer reservationId;
    private Integer hostId;
    private Integer tenantId;
    private String tenantWallet;
    private String hostWallet;
    private Double amount;
    private Double totalAmount;
    private LocalDateTime timestamp; // ✅ AJOUTER
    private Map<String, Object> metadata;

    // ✅ AJOUTER un enum
    public enum PaymentMessageType {
        INITIATE_PAYMENT,
        RELEASE_ESCROW,
        REFUND,
        PAYMENT_CONFIRMED,
        PAYMENT_FAILED
    }

    // Méthodes factory améliorées
    public static PaymentMessage initiatePayment(
            Integer reservationId,
            Integer hostId,
            Integer tenantId,
            String hostWallet,
            String tenantWallet,
            Double amount) {
        return PaymentMessage.builder()
                .type(PaymentMessageType.INITIATE_PAYMENT)
                .reservationId(reservationId)
                .hostId(hostId)
                .tenantId(tenantId)
                .hostWallet(hostWallet)
                .tenantWallet(tenantWallet)
                .amount(amount)
                .totalAmount(amount * 1.05) // +5% frais
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static PaymentMessage releaseEscrow(
            Integer reservationId,
            Integer hostId,
            Integer tenantId,
            String hostWallet,
            String tenantWallet,
            Double amount) {
        return PaymentMessage.builder()
                .type(PaymentMessageType.RELEASE_ESCROW)
                .reservationId(reservationId)
                .hostId(hostId)
                .tenantId(tenantId)
                .hostWallet(hostWallet)
                .tenantWallet(tenantWallet)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static PaymentMessage refund(
            Integer reservationId,
            Integer hostId,
            Integer tenantId,
            String hostWallet,
            String tenantWallet,
            Double amount) {
        return PaymentMessage.builder()
                .type(PaymentMessageType.REFUND)
                .reservationId(reservationId)
                .hostId(hostId)
                .tenantId(tenantId)
                .hostWallet(hostWallet)
                .tenantWallet(tenantWallet)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static PaymentMessage paymentConfirmed(
            Integer reservationId,
            Double amount,
            String txHash) {
        return PaymentMessage.builder()
                .type(PaymentMessageType.PAYMENT_CONFIRMED)
                .reservationId(reservationId)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .metadata(Map.of("transactionHash", txHash))
                .build();
    }

    public static PaymentMessage paymentFailed(
            Integer reservationId,
            String reason) {
        return PaymentMessage.builder()
                .type(PaymentMessageType.PAYMENT_FAILED)
                .reservationId(reservationId)
                .timestamp(LocalDateTime.now())
                .metadata(Map.of("reason", reason))
                .build();
    }
}