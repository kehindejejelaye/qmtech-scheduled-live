package com.qmtech.scheduledlive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PollCreateRequest {
    @NotBlank
    private String question;
    @NotEmpty
    private List<@NotBlank String> options;
    @NotNull
    private Instant scheduledStartTime;
}

