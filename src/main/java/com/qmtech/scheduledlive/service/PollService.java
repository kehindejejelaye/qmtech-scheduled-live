package com.qmtech.scheduledlive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qmtech.scheduledlive.config.RabbitConfig;
import com.qmtech.scheduledlive.dto.PollCreateRequest;
import com.qmtech.scheduledlive.dto.PollResponse;
import com.qmtech.scheduledlive.dto.VoteEvent;
import com.qmtech.scheduledlive.dto.VoteRequest;
import com.qmtech.scheduledlive.entity.Poll;
import com.qmtech.scheduledlive.repo.PollRepository;
import com.qmtech.scheduledlive.repo.VoteRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PollService {

    private final PollRepository pollRepository;
    private final ObjectMapper objectMapper;
    private final VoteRepository voteRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public PollService(PollRepository pollRepository,
                       ObjectMapper objectMapper,
                       VoteRepository voteRepository,
                       RabbitTemplate rabbitTemplate) {
        this.pollRepository = pollRepository;
        this.objectMapper = objectMapper;
        this.voteRepository = voteRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Poll createPoll(PollCreateRequest request) {
        try {
            String optionsJson = objectMapper.writeValueAsString(request.getOptions());

            Poll poll = new Poll();
            poll.setQuestion(request.getQuestion());
            poll.setScheduledStartTime(request.getScheduledStartTime());
            poll.setOptionsJson(optionsJson);

            return pollRepository.save(poll);
        } catch (JsonProcessingException e) {
            // Log the error and throw a custom exception or a RuntimeException
            // For now, we'll just rethrow a RuntimeException.
            throw new RuntimeException("Error processing poll options JSON", e);
        }
    }

    @Transactional(readOnly = true)
    public PollResponse getPollWithTallies(UUID pollId) throws JsonProcessingException {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found with ID: " + pollId));

        // 1. Get the vote counts
        List<Object[]> voteCounts = voteRepository.countVotesByOption(pollId);
        Map<String, Long> tallies = voteCounts.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        // 2. Deserialize the JSON options
        List<String> options = objectMapper.readValue(poll.getOptionsJson(), new TypeReference<List<String>>() {});

        // 3. Map the entity and tallies to the response DTO
        PollResponse response = new PollResponse();
        response.setId(poll.getId());
        response.setQuestion(poll.getQuestion());
        response.setOptions(options);
        response.setTallies(tallies);
        response.setScheduledStartTime(poll.getScheduledStartTime());

        return response;
    }

    public Map<String, Long> computeTallies(UUID pollId) {
        List<Object[]> voteCounts = voteRepository.countVotesByOption(pollId);

        return voteCounts.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],   // option name
                        row -> (Long) row[1]     // count
                ));
    }

    @Transactional(readOnly = true)
    public void castVote(UUID pollId, VoteRequest request) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found"));

        // Validate poll has started
        if (Instant.now().isBefore(poll.getScheduledStartTime())) {
            throw new IllegalStateException("Poll has not started yet");
        }

        // Validate option
        try {
            List<String> options = objectMapper.readValue(
                    poll.getOptionsJson(),
                    new TypeReference<List<String>>() {});
            if (!options.contains(request.getOption())) {
                throw new IllegalArgumentException("Invalid option");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to read poll options", e);
        }

        // Publish event to RabbitMQ
        VoteEvent event = new VoteEvent(
                pollId,
                request.getUsername(),
                request.getOption(),
                Instant.now()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                event
        );
    }

    @Transactional(readOnly = true)
    public List<PollResponse> getAllPolls() {
        return pollRepository.findAll()
                .stream()
                .map(p -> {
                    try {
                        List<String> options =
                                objectMapper.readValue(p.getOptionsJson(), new TypeReference<List<String>>() {});
                        PollResponse r = new PollResponse();
                        r.setId(p.getId());
                        r.setQuestion(p.getQuestion());
                        r.setOptions(options);
                        r.setScheduledStartTime(p.getScheduledStartTime());
                        return r;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to read poll options", e);
                    }
                })
                .toList();
    }

}
