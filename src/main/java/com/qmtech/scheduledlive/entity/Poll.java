package com.qmtech.scheduledlive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "poll")
@Data
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String question;

    // Use native Hibernate 6 JSON support
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="options", columnDefinition = "jsonb", nullable = false)
    private String optionsJson;

    @Column(name = "scheduled_start_time", nullable = false)
    private Instant scheduledStartTime;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Vote> votes;
}