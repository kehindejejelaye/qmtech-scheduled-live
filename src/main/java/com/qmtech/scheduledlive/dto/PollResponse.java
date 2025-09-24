package com.qmtech.scheduledlive.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class PollResponse {
    private UUID id;
    private String question;
    private List<String> options;
    private Map<String, Long> tallies; // option -> count
    private Instant scheduledStartTime;
}