package org.example.greduatebe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebSocket 연결/해제 이벤트 리스너
 * 연결, 구독, 해제 이벤트를 로깅합니다
 */
@Component
@Slf4j
public class WebSocketEventListener {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * WebSocket 연결 시작 이벤트 (핸드셰이크 전)
     */
    @EventListener
    public void handleWebSocketConnectingListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String time = LocalDateTime.now().format(TIME_FORMATTER);

        log.info("🔵 [{}] [WebSocket] Connection attempt started - sessionId: {}", time, sessionId);
    }

    /**
     * WebSocket 연결 성공 이벤트
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String time = LocalDateTime.now().format(TIME_FORMATTER);

        log.info("🟢 [{}] [WebSocket] ✅ Connection established successfully - sessionId: {}", time, sessionId);
        log.debug("Connection details - User: {}, Session attributes: {}",
                headerAccessor.getUser(), headerAccessor.getSessionAttributes());
    }

    /**
     * WebSocket 구독 이벤트
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        String subscriptionId = headerAccessor.getSubscriptionId();
        String time = LocalDateTime.now().format(TIME_FORMATTER);

        log.info("📡 [{}] [WebSocket] Client subscribed - sessionId: {}, destination: {}, subscriptionId: {}",
                time, sessionId, destination, subscriptionId);
    }

    /**
     * WebSocket 구독 해제 이벤트
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String subscriptionId = headerAccessor.getSubscriptionId();
        String time = LocalDateTime.now().format(TIME_FORMATTER);

        log.info("📴 [{}] [WebSocket] Client unsubscribed - sessionId: {}, subscriptionId: {}",
                time, sessionId, subscriptionId);
    }

    /**
     * WebSocket 연결 해제 이벤트
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String time = LocalDateTime.now().format(TIME_FORMATTER);

        log.info("🔴 [{}] [WebSocket] ❌ Connection closed - sessionId: {}, closeStatus: {}",
                time, sessionId, event.getCloseStatus());
    }
}
