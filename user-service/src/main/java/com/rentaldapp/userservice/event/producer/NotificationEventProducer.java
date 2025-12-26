package com.rentaldapp.userservice.event.producer;

import com.rentaldapp.userservice.event.EmailVerificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification:notification.exchange}")
    private String notificationExchange;

    // CORRECTION : Utiliser la bonne routing key pour notification queue
    @Value("${rabbitmq.routing.key.notification:notification.routing.key}")
    private String notificationRoutingKey;

    public void sendEmailVerificationEvent(EmailVerificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    notificationExchange,
                    notificationRoutingKey, // CORRECT: utiliser notification.routing.key
                    event
            );
            System.out.println("✅ Email verification event sent to notification.queue: " + event.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email verification event: " + e.getMessage());
        }
    }
}