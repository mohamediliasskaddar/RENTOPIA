package com.rental.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // ========== EXCHANGES ==========
    public static final String EXCHANGE_NAME = "rental.exchange";
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String BLOCKCHAIN_EXCHANGE = "blockchain.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Exchange pour les messages échoués
    public static final String DLX_EXCHANGE = "rental.dlx.exchange";

    // ========== QUEUES PRINCIPALES ==========
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String BOOKING_QUEUE = "booking.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String BLOCKCHAIN_QUEUE = "blockchain.queue";

    // ========== QUEUES ÉVÉNEMENTS ==========
    public static final String PAYMENT_CREATED_QUEUE = "payment.created.queue";
    public static final String PAYMENT_CONFIRMED_QUEUE = "payment.confirmed.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";
    public static final String ESCROW_RELEASED_QUEUE = "escrow.released.queue";
    public static final String REFUND_PROCESSED_QUEUE = "refund.processed.queue";

    // Dead Letter Queue
    public static final String DLQ_QUEUE = "payment.dlq.queue";

    // ========== ROUTING KEYS ==========
    public static final String PAYMENT_ROUTING_KEY = "payment.#";
    public static final String BOOKING_ROUTING_KEY = "booking.#";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.#";
    public static final String BLOCKCHAIN_ROUTING_KEY = "blockchain.#";

    public static final String PAYMENT_CREATED_KEY = "payment.created";
    public static final String PAYMENT_CONFIRMED_KEY = "payment.confirmed";
    public static final String PAYMENT_FAILED_KEY = "payment.failed";
    public static final String ESCROW_RELEASED_KEY = "escrow.released";
    public static final String REFUND_PROCESSED_KEY = "refund.processed";
    public static final String DLQ_ROUTING_KEY = "payment.dlq";

    // ========== EXCHANGES ==========
    @Bean
    public TopicExchange rentalExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange blockchainExchange() {
        return new TopicExchange(BLOCKCHAIN_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ========== QUEUES AVEC DLX ==========

    /**
     * Configuration pour créer une queue avec Dead Letter Exchange
     */
    private Queue createQueueWithDLX(String queueName) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        args.put("x-message-ttl", 60000); // TTL de 60 secondes (optionnel)
        return new Queue(queueName, true, false, false, args);
    }

    @Bean
    public Queue paymentQueue() {
        return createQueueWithDLX(PAYMENT_QUEUE);
    }

    @Bean
    public Queue bookingQueue() {
        return createQueueWithDLX(BOOKING_QUEUE);
    }

    @Bean
    public Queue notificationQueue() {
        return createQueueWithDLX(NOTIFICATION_QUEUE);
    }

    @Bean
    public Queue blockchainQueue() {
        return createQueueWithDLX(BLOCKCHAIN_QUEUE);
    }

    @Bean
    public Queue paymentCreatedQueue() {
        return createQueueWithDLX(PAYMENT_CREATED_QUEUE);
    }

    @Bean
    public Queue paymentConfirmedQueue() {
        return createQueueWithDLX(PAYMENT_CONFIRMED_QUEUE);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return createQueueWithDLX(PAYMENT_FAILED_QUEUE);
    }

    @Bean
    public Queue escrowReleasedQueue() {
        return createQueueWithDLX(ESCROW_RELEASED_QUEUE);
    }

    @Bean
    public Queue refundProcessedQueue() {
        return createQueueWithDLX(REFUND_PROCESSED_QUEUE);
    }

    // Dead Letter Queue (sans DLX pour éviter les boucles)
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    // ========== BINDINGS ==========

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(rentalExchange())
                .with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public Binding bookingBinding() {
        return BindingBuilder.bind(bookingQueue())
                .to(rentalExchange())
                .with(BOOKING_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(rentalExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding blockchainBinding() {
        return BindingBuilder.bind(blockchainQueue())
                .to(rentalExchange())
                .with(BLOCKCHAIN_ROUTING_KEY);
    }

    @Bean
    public Binding paymentCreatedBinding() {
        return BindingBuilder.bind(paymentCreatedQueue())
                .to(rentalExchange())
                .with(PAYMENT_CREATED_KEY);
    }

    @Bean
    public Binding paymentConfirmedBinding() {
        return BindingBuilder.bind(paymentConfirmedQueue())
                .to(rentalExchange())
                .with(PAYMENT_CONFIRMED_KEY);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(rentalExchange())
                .with(PAYMENT_FAILED_KEY);
    }

    @Bean
    public Binding escrowReleasedBinding() {
        return BindingBuilder.bind(escrowReleasedQueue())
                .to(rentalExchange())
                .with(ESCROW_RELEASED_KEY);
    }

    @Bean
    public Binding refundProcessedBinding() {
        return BindingBuilder.bind(refundProcessedQueue())
                .to(rentalExchange())
                .with(REFUND_PROCESSED_KEY);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    // ========== MESSAGE CONVERTER ==========

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setExchange(EXCHANGE_NAME);

        // Configuration du retry automatique
        template.setMandatory(true);

        return template;
    }

    /**
     * Message Recoverer pour gérer les messages qui échouent après tous les retries
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(
                rabbitTemplate,
                DLX_EXCHANGE,
                DLQ_ROUTING_KEY
        );
    }
}