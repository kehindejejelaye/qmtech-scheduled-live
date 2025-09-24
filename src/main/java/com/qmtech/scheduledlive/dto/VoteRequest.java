package com.qmtech.scheduledlive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VoteRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String option;
}