package org.example.greduatebe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * WebSocket 채널 Interceptor
 * STOMP 프레임 레벨에서 모든 메시지를 로깅합니다
 */
@Component
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command != null) {
            switch (command) {
                case CONNECT:
                    log.info("📨 [STOMP] CONNECT frame received");
                    log.debug("   - Session: {}", accessor.getSessionId());
                    log.debug("   - User: {}", accessor.getUser());
                    break;

                case SUBSCRIBE:
                    log.info("📨 [STOMP] SUBSCRIBE frame received");
                    log.info("   - Destination: {}", accessor.getDestination());
                    log.info("   - Subscription ID: {}", accessor.getSubscriptionId());
                    log.info("   - Session: {}", accessor.getSessionId());
                    break;

                case SEND:
                    log.info("📨 [STOMP] SEND frame received - MESSAGE FROM CLIENT");
                    log.info("   - Destination: {}", accessor.getDestination());
                    log.info("   - Session: {}", accessor.getSessionId());
                    log.info("   - Message Payload Type: {}", message.getPayload().getClass().getName());
                    log.info("   - Message Payload: {}", message.getPayload());
                    break;

                case DISCONNECT:
                    log.info("📨 [STOMP] DISCONNECT frame received");
                    log.info("   - Session: {}", accessor.getSessionId());
                    break;

                case UNSUBSCRIBE:
                    log.info("📨 [STOMP] UNSUBSCRIBE frame received");
                    log.info("   - Subscription ID: {}", accessor.getSubscriptionId());
                    break;

                default:
                    log.debug("📨 [STOMP] {} frame received", command);
                    break;
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command != null && command == StompCommand.MESSAGE) {
            log.debug("📤 [STOMP] MESSAGE frame sent to client");
            log.debug("   - Destination: {}", accessor.getDestination());
        }
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            log.error("❌ [STOMP] Error sending message - command: {}, error: {}",
                    accessor.getCommand(), ex.getMessage(), ex);
        }
    }
}
