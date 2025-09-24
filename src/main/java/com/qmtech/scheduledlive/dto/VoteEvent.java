package com.qmtech.scheduledlive.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a vote message sent to RabbitMQ.
 * This is the payload published by VoteProducer
 * and consumed by VoteConsumer.
 */
@Data
public class VoteEvent {

    private UUID pollId;
    private String username;
    private String option;
    private Instant timestamp;

    // Default constructor required for Jackson
    public VoteEvent() {
    }

    public VoteEvent(UUID pollId, String username, String option, Instant timestamp) {
        this.pollId = pollId;
        this.username = username;
        this.option = option;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "VoteEvent{" +
                "pollId=" + pollId +
                ", username='" + username + '\'' +
                ", option='" + option + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
