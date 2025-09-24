package com.qmtech.scheduledlive.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "vote.exchange";
    public static final String QUEUE_NAME    = "vote.queue";
    public static final String ROUTING_KEY   = "vote.key";

    @Bean
    public DirectExchange voteExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue voteQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding voteBinding(Queue voteQueue, DirectExchange voteExchange) {
        return BindingBuilder
                .bind(voteQueue)
                .to(voteExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}