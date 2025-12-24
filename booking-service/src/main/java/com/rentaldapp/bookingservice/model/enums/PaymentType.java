package com.rentaldapp.bookingservice.model.enums;

public enum PaymentType {
    BOOKING_PAYMENT,      // Paiement de réservation
    REFUND,               // Remboursement
    ESCROW_RELEASE,       // Libération escrow au propriétaire
    PLATFORM_FEE         // Frais de plateforme
}