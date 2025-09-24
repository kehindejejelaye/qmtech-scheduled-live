package com.qmtech.scheduledlive.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qmtech.scheduledlive.entity.Poll;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PollWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    /** pollId -> set of sessions */
    private final Map<UUID, Set<WebSocketSession>> subscribers = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<?,?> payload = objectMapper.readValue(message.getPayload(), Map.class);
        if ("subscribe".equals(payload.get("action"))) {
            UUID pollId = UUID.fromString(payload.get("pollId").toString());
            subscribers.computeIfAbsent(pollId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        subscribers.values().forEach(set -> set.remove(session));
    }

    /** Broadcast updated tallies to all subscribers of a poll. */
//    public void broadcastToPoll(UUID pollId, Poll poll, Map<String, Long> tallies) {
//        Set<WebSocketSession> sessions = subscribers.getOrDefault(pollId, Collections.emptySet());
//        if (sessions.isEmpty()) return;
//
//        Map<String, Object> payload = Map.of(
//                "type", "poll_update",
//                "pollId", pollId,
//                "question", poll.getQuestion(),
//                "tallies", tallies
//        );
//        try {
//            String json = objectMapper.writeValueAsString(payload);
//            TextMessage msg = new TextMessage(json);
//            for (WebSocketSession s : sessions) {
//                if (s.isOpen()) s.sendMessage(msg);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public void broadcastToPoll(UUID pollId, Poll poll, Map<String, Long> tallies) {
        Set<WebSocketSession> sessions = subscribers.getOrDefault(pollId, Collections.emptySet());
        if (sessions.isEmpty()) return;

        try {
            // ✅ Get options from poll
            List<String> options = objectMapper.readValue(
                    poll.getOptionsJson(),
                    new TypeReference<List<String>>() {}
            );

            // ✅ Build full payload
            Map<String, Object> payload = Map.of(
                    "type", "poll_update",
                    "id", poll.getId(),  // use 'id' instead of 'pollId'
                    "question", poll.getQuestion(),
                    "options", options,
                    "tallies", tallies,
                    "scheduledStartTime", poll.getScheduledStartTime()
            );

            String json = objectMapper.writeValueAsString(payload);
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) s.sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
