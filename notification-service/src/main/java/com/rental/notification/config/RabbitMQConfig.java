package com.rental.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

@Configuration
@Slf4j
public class RabbitMQConfig {

    @Bean
    public Queue notificationQueue() {
        log.info("📨 Création de la queue: notification.queue");
        return new Queue("notification.queue", true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        log.info("🔄 Création de l'exchange: notification.exchange");
        return new TopicExchange("notification.exchange");
    }

    @Bean
    public Binding notificationBinding() {
        log.info("🔗 Création du binding entre queue et exchange");
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with("notification.routing.key");
    }

    @Bean
    public SimpleMessageConverter debugMessageConverter() {
        return new SimpleMessageConverter() {
            @Override
            public Object fromMessage(org.springframework.amqp.core.Message message)
                    throws MessageConversionException {

                byte[] body = message.getBody();
                String contentType = message.getMessageProperties().getContentType();

                log.info("📥 Message reçu - Taille: {} bytes, Content-Type: {}",
                        body.length, contentType);

                try {
                    // Essayer de désérialiser l'objet Java
                    if (contentType != null && contentType.contains("application/x-java-serialized-object")) {
                        return deserializeJavaObject(body);
                    }

                    // Sinon, utiliser le comportement par défaut
                    return super.fromMessage(message);

                } catch (Exception e) {
                    log.error("❌ Erreur lors de la conversion: {}", e.getMessage());

                    // Fallback: essayer d'extraire manuellement
                    return extractDataFromBytes(body);
                }
            }

            @SuppressWarnings("unchecked")
            private Object deserializeJavaObject(byte[] data) throws Exception {
                try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {

                    Object obj = ois.readObject();
                    log.info("✅ Objet Java désérialisé: {}", obj.getClass().getName());

                    if (obj instanceof Map) {
                        return obj;
                    }

                    // Convertir en Map simple
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("rawObject", obj.toString());
                    result.put("className", obj.getClass().getName());
                    result.put("type", "JAVA_SERIALIZED_OBJECT");

                    return result;
                }
            }

            private Map<String, Object> extractDataFromBytes(byte[] data) {
                Map<String, Object> result = new java.util.HashMap<>();
                String content = new String(data);

                log.debug("Contenu brut: {}...", content.substring(0, Math.min(100, content.length())));

                // Extraction manuelle
                if (content.contains("mina26bouzid@gmail.com")) {
                    result.put("email", "mina26bouzid@gmail.com");
                }

                // Extraire UUID
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "pt \\$([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})"
                );
                java.util.regex.Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    result.put("verificationToken", matcher.group(1));
                }

                result.put("type", "EMAIL_VERIFICATION");
                result.put("source", "DEBUG_CONVERTER");

                return result;
            }
        };
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            SimpleMessageConverter debugMessageConverter) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(debugMessageConverter);

        // Callback pour le succès/échec de l'envoi
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("✅ Message envoyé avec succès");
            } else {
                log.error("❌ Échec d'envoi du message: {}", cause);
            }
        });

        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleMessageConverter debugMessageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(debugMessageConverter);

        // Configuration de debug
        factory.setConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);

        return factory;
    }

    @Bean
    public Declarables declarables() {
        return new Declarables(
                notificationQueue(),
                notificationExchange(),
                notificationBinding()
        );
    }
}