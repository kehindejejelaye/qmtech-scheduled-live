package com.qmtech.scheduledlive.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qmtech.scheduledlive.dto.PollCreateRequest;
import com.qmtech.scheduledlive.dto.PollResponse;
import com.qmtech.scheduledlive.dto.VoteRequest;
import com.qmtech.scheduledlive.entity.Poll;
import com.qmtech.scheduledlive.service.PollService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/polls")
public class PollController {

    private final PollService pollService;

    @Autowired
    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @PostMapping
    public ResponseEntity<Poll> createPoll(@Valid @RequestBody PollCreateRequest pollCreateRequest) {
        Poll createdPoll = pollService.createPoll(pollCreateRequest);
        return new ResponseEntity<>(createdPoll, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PollResponse> getPollWithTallies(@PathVariable UUID id) throws JsonProcessingException {
        PollResponse pollResponse = pollService.getPollWithTallies(id);
        return ResponseEntity.ok(pollResponse);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> castVote(
            @PathVariable UUID id,
            @Valid @RequestBody VoteRequest voteRequest) {

        pollService.castVote(id, voteRequest);
        return ResponseEntity.accepted().build(); // 202 Accepted since processing is async
    }

    @GetMapping
    public ResponseEntity<List<PollResponse>> getAllPolls() {
        List<PollResponse> polls = pollService.getAllPolls();
        return ResponseEntity.ok(polls);
    }
}
