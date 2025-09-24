package com.qmtech.scheduledlive.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "vote")
@Data
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // rest of the fields remain the same
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(nullable = false)
    private String username;

    @Column(name = "option_chosen", nullable = false)
    private String optionChosen;

    @Column(name = "created_at")
    private Instant createdAt;
}