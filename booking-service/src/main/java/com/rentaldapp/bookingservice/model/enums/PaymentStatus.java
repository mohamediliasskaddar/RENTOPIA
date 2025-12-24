package com.rentaldapp.bookingservice.model.enums;

public enum PaymentStatus {
    PENDING,           // En attente
    PROCESSING,        // En cours de traitement
    CONFIRMED,         // Confirmé
    FAILED,            // Échoué
    REFUNDED          // Remboursé
}