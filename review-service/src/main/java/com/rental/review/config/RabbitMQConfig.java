package com.rental.review.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Noms des queues
    public static final String REVIEW_QUEUE = "review.queue";
    public static final String REVIEW_CREATED_QUEUE = "review.created.queue";

    // Exchange
    public static final String REVIEW_EXCHANGE = "review.exchange";

    // Routing keys
    public static final String REVIEW_ROUTING_KEY = "review.routing.key";
    public static final String REVIEW_CREATED_KEY = "review.created";

    @Bean
    public Queue reviewQueue() {
        return new Queue(REVIEW_QUEUE, true);
    }

    @Bean
    public Queue reviewCreatedQueue() {
        return new Queue(REVIEW_CREATED_QUEUE, true);
    }

    @Bean
    public TopicExchange reviewExchange() {
        return new TopicExchange(REVIEW_EXCHANGE);
    }

    @Bean
    public Binding reviewBinding() {
        return BindingBuilder
                .bind(reviewQueue())
                .to(reviewExchange())
                .with(REVIEW_ROUTING_KEY);
    }

    @Bean
    public Binding reviewCreatedBinding() {
        return BindingBuilder
                .bind(reviewCreatedQueue())
                .to(reviewExchange())
                .with(REVIEW_CREATED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}