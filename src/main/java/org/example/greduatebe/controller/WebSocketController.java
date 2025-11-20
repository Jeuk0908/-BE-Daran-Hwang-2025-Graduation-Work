package org.example.greduatebe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.request.WebSocketMessage;
import org.example.greduatebe.entity.MissionEvent;
import org.example.greduatebe.service.EventService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 메시지 핸들러 Controller
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final EventService eventService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 미션 이벤트 수신 핸들러
     * 클라이언트가 /app/mission/event로 메시지를 보내면 이 메서드가 호출됩니다
     *
     * @param message WebSocket 메시지
     * @return ACK 응답
     */
    @MessageMapping("/mission/event")
    public void handleMissionEvent(WebSocketMessage message) {
        long startTime = System.currentTimeMillis();

        log.info("📥 [WebSocket] Message received - eventType: {}, attemptId: {}, sessionId: {}",
                message.getEventType(), message.getAttemptId(), message.getSessionId());
        log.debug("Message data: {}", message.getData());

        try {
            // 이벤트 처리
            MissionEvent savedEvent = eventService.processEvent(message);
            long processingTime = System.currentTimeMillis() - startTime;

            log.info("✅ [WebSocket] Event processed successfully - eventId: {}, processingTime: {}ms",
                    savedEvent.getEventId(), processingTime);

            // ACK 응답 생성
            Map<String, Object> ackResponse = new HashMap<>();
            ackResponse.put("status", "success");
            ackResponse.put("eventId", savedEvent.getEventId());
            ackResponse.put("eventType", savedEvent.getEventType());
            ackResponse.put("attemptId", savedEvent.getAttemptId());
            ackResponse.put("timestamp", LocalDateTime.now());
            ackResponse.put("processingTime", savedEvent.getProcessingTime());

            // 클라이언트에게 ACK 응답 전송 (개별 세션)
            String ackDestination = "/topic/mission/" + message.getAttemptId() + "/ack";
            messagingTemplate.convertAndSend(ackDestination, ackResponse);
            log.info("📤 [WebSocket] ACK sent to client - destination: {}, eventId: {}",
                    ackDestination, savedEvent.getEventId());

        } catch (Exception e) {
            log.error("❌ [WebSocket] Error processing message - eventType: {}, attemptId: {}, error: {}",
                    message.getEventType(), message.getAttemptId(), e.getMessage(), e);

            // 에러 응답 생성
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("eventType", message.getEventType());
            errorResponse.put("attemptId", message.getAttemptId());
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            // 클라이언트에게 에러 응답 전송
            String errorDestination = "/topic/mission/" + message.getAttemptId() + "/error";
            messagingTemplate.convertAndSend(errorDestination, errorResponse);

            log.error("📤 [WebSocket] Error response sent to client - destination: {}, attemptId: {}",
                    errorDestination, message.getAttemptId());
        }
    }
}
