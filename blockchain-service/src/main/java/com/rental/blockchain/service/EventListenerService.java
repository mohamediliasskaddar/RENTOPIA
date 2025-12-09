package com.rental.blockchain.service;

import com.rental.blockchain.contract.RentalPlatform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.gas.StaticGasProvider;

import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.HashMap;
import java.util.Map;

/**
 * Service d'écoute des événements blockchain
 *
 * Écoute les événements RÉELS du smart contract:
 * - BookingCreated
 * - CheckInCompleted (pas CheckedIn)
 * - CheckOutCompleted (pas CheckedOut)
 * - PaymentReleased (pas FundsReleased)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventListenerService {

    private final Web3j web3j;
    private final Credentials adminCredentials;
    private final StaticGasProvider gasProvider;
    private final RabbitTemplate rabbitTemplate;

    @Value("${ethereum.contract.address}")
    private String contractAddress;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    private RentalPlatform contract;

    // Subscriptions pour chaque événement
    private Disposable bookingCreatedSubscription;
    private Disposable checkInCompletedSubscription;
    private Disposable checkOutCompletedSubscription;
    private Disposable paymentReleasedSubscription;

    /**
     * Initialisation au démarrage du service
     */
    @PostConstruct
    public void init() {
        log.info("🎧 Initialisation du listener d'événements blockchain...");

        try {
            // Charger le contrat
            contract = RentalPlatform.load(
                    contractAddress,
                    web3j,
                    adminCredentials,
                    gasProvider
            );

            // Démarrer l'écoute
            startListening();
        } catch (Exception e) {
            log.error("❌ Erreur initialisation listener: {}", e.getMessage(), e);
        }
    }

    /**
     * Démarrer l'écoute de tous les événements
     */
    private void startListening() {
        log.info("👂 Démarrage de l'écoute des événements...");

        try {
            // Écouter BookingCreated
            bookingCreatedSubscription = contract.bookingCreatedEventFlowable(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST
            ).subscribe(
                    event -> handleBookingCreated(event),
                    error -> log.error("❌ Erreur BookingCreated: {}", error.getMessage())
            );

            // Écouter CheckInCompleted (pas CheckedIn!)
            checkInCompletedSubscription = contract.checkInCompletedEventFlowable(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST
            ).subscribe(
                    event -> handleCheckInCompleted(event),
                    error -> log.error("❌ Erreur CheckInCompleted: {}", error.getMessage())
            );

            // Écouter CheckOutCompleted (pas CheckedOut!)
            checkOutCompletedSubscription = contract.checkOutCompletedEventFlowable(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST
            ).subscribe(
                    event -> handleCheckOutCompleted(event),
                    error -> log.error("❌ Erreur CheckOutCompleted: {}", error.getMessage())
            );

            // Écouter PaymentReleased (pas FundsReleased!)
            paymentReleasedSubscription = contract.paymentReleasedEventFlowable(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST
            ).subscribe(
                    event -> handlePaymentReleased(event),
                    error -> log.error("❌ Erreur PaymentReleased: {}", error.getMessage())
            );

            log.info("✅ Écoute des événements démarrée avec succès!");
            log.info("   - BookingCreated ✓");
            log.info("   - CheckInCompleted ✓");
            log.info("   - CheckOutCompleted ✓");
            log.info("   - PaymentReleased ✓");

        } catch (Exception e) {
            log.error("❌ Erreur démarrage écoute: {}", e.getMessage(), e);
        }
    }

    /**
     * Gérer l'événement BookingCreated
     */
    private void handleBookingCreated(RentalPlatform.BookingCreatedEventResponse event) {
        log.info("📢 EVENT: BookingCreated");
        log.info("   📌 Booking ID: {}", event.bookingId);
        log.info("   👤 Tenant: {}", event.tenant);
        log.info("   🏠 Property ID: {}", event.propertyId);
        log.info("   💰 Total Amount: {} wei", event.totalAmount);

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("blockchainBookingId", event.bookingId.longValue());
            message.put("tenant", event.tenant);
            message.put("propertyId", event.propertyId.longValue());
            message.put("totalAmount", event.totalAmount.toString());
            message.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(exchange, "booking.confirmed", message);
            log.info("📤 Message envoyé à RabbitMQ: booking.confirmed");
        } catch (Exception e) {
            log.error("❌ Erreur envoi RabbitMQ: {}", e.getMessage(), e);
        }
    }

    /**
     * Gérer l'événement CheckInCompleted
     */
    private void handleCheckInCompleted(RentalPlatform.CheckInCompletedEventResponse event) {
        log.info("📢 EVENT: CheckInCompleted");
        log.info("   📌 Booking ID: {}", event.bookingId);
        log.info("   ⏰ Timestamp: {}", event.timestamp);

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("blockchainBookingId", event.bookingId.longValue());
            message.put("checkInTimestamp", event.timestamp.longValue());
            message.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(exchange, "checkin.completed", message);
            log.info("📤 Message envoyé à RabbitMQ: checkin.completed");
        } catch (Exception e) {
            log.error("❌ Erreur envoi RabbitMQ: {}", e.getMessage(), e);
        }
    }

    /**
     * Gérer l'événement CheckOutCompleted
     */
    private void handleCheckOutCompleted(RentalPlatform.CheckOutCompletedEventResponse event) {
        log.info("📢 EVENT: CheckOutCompleted");
        log.info("   📌 Booking ID: {}", event.bookingId);
        log.info("   ⏰ Timestamp: {}", event.timestamp);

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("blockchainBookingId", event.bookingId.longValue());
            message.put("checkOutTimestamp", event.timestamp.longValue());
            message.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(exchange, "checkout.completed", message);
            log.info("📤 Message envoyé à RabbitMQ: checkout.completed");
        } catch (Exception e) {
            log.error("❌ Erreur envoi RabbitMQ: {}", e.getMessage(), e);
        }
    }

    /**
     * Gérer l'événement PaymentReleased
     */
    private void handlePaymentReleased(RentalPlatform.PaymentReleasedEventResponse event) {
        log.info("📢 EVENT: PaymentReleased");
        log.info("   📌 Booking ID: {}", event.bookingId);
        log.info("   👤 Owner: {}", event.owner);
        log.info("   💰 Amount: {} wei", event.amount);

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("blockchainBookingId", event.bookingId.longValue());
            message.put("owner", event.owner);
            message.put("amount", event.amount.toString());
            message.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(exchange, "payment.released", message);
            log.info("📤 Message envoyé à RabbitMQ: payment.released");
        } catch (Exception e) {
            log.error("❌ Erreur envoi RabbitMQ: {}", e.getMessage(), e);
        }
    }

    /**
     * Nettoyage lors de l'arrêt du service
     */
    @PreDestroy
    public void cleanup() {
        log.info("🛑 Arrêt du listener d'événements...");

        if (bookingCreatedSubscription != null && !bookingCreatedSubscription.isDisposed()) {
            bookingCreatedSubscription.dispose();
            log.info("   ✓ BookingCreated subscription arrêtée");
        }

        if (checkInCompletedSubscription != null && !checkInCompletedSubscription.isDisposed()) {
            checkInCompletedSubscription.dispose();
            log.info("   ✓ CheckInCompleted subscription arrêtée");
        }

        if (checkOutCompletedSubscription != null && !checkOutCompletedSubscription.isDisposed()) {
            checkOutCompletedSubscription.dispose();
            log.info("   ✓ CheckOutCompleted subscription arrêtée");
        }

        if (paymentReleasedSubscription != null && !paymentReleasedSubscription.isDisposed()) {
            paymentReleasedSubscription.dispose();
            log.info("   ✓ PaymentReleased subscription arrêtée");
        }

        log.info("✅ Listener arrêté proprement");
    }
}