package com.qmtech.scheduledlive.service;


import com.qmtech.scheduledlive.config.RabbitConfig;
import com.qmtech.scheduledlive.dto.VoteEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VoteProducer {

    @Autowired
    private final RabbitTemplate rabbitTemplate;

    public VoteProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Publish a vote event to RabbitMQ for asynchronous processing. */
    public void publish(VoteEvent voteEvent) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                voteEvent
        );
    }
}
