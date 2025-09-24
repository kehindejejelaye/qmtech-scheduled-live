package com.qmtech.scheduledlive.config;

import com.qmtech.scheduledlive.websocket.PollWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PollWebSocketHandler pollWebSocketHandler;

    public WebSocketConfig(PollWebSocketHandler pollWebSocketHandler) {
        this.pollWebSocketHandler = pollWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(pollWebSocketHandler, "/ws/poll")
                .setAllowedOrigins("*"); // Configure CORS as needed
    }
}
