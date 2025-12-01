package com.rental.notification;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RabbitMQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testRabbitMQConnection() {
        try {
            rabbitTemplate.convertAndSend("test.queue", "Hello RabbitMQ!");
            System.out.println("✅ Message envoyé avec succès à RabbitMQ!");
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion à RabbitMQ: " + e.getMessage());
        }
    }
}