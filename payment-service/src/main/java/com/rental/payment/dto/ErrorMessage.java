// src/main/java/com/rental/payment/dto/ErrorMessage.java
package com.rental.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorMessage {
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String path;              // AJOUTÉ: chemin de l'API
    private Integer status;           // AJOUTÉ: code HTTP

    // Méthodes factory pour les erreurs courantes
    public static ErrorMessage paymentError(String message, String path) {
        return ErrorMessage.builder()
                .error("Payment Error")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .status(400)
                .build();
    }

    public static ErrorMessage insufficientBalance(String message, String path) {
        return ErrorMessage.builder()
                .error("Insufficient Balance")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .status(402)  // Payment Required
                .build();
    }

    public static ErrorMessage serviceUnavailable(String serviceName, String path) {
        return ErrorMessage.builder()
                .error("Service Unavailable")
                .message(serviceName + " est temporairement indisponible")
                .timestamp(LocalDateTime.now())
                .path(path)
                .status(503)
                .build();
    }

    public static ErrorMessage validationError(String message, String path) {
        return ErrorMessage.builder()
                .error("Validation Error")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .status(422)  // Unprocessable Entity
                .build();
    }
}