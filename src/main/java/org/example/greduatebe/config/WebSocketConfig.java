package org.example.greduatebe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 Configuration
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    private final WebSocketChannelInterceptor channelInterceptor;

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트는 이 엔드포인트로 WebSocket 연결을 시도합니다
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoints");

        // Native WebSocket 지원 (ws:// 프로토콜)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 설정 (운영에서는 구체적인 도메인 지정 권장)
                .addInterceptors(handshakeInterceptor);

        // SockJS 지원 (HTTP 폴링 fallback)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
    }

    /**
     * Message Broker 설정
     * - /topic: 메시지 브로커가 구독자에게 메시지를 전달하는 prefix
     * - /app: 클라이언트가 메시지를 보낼 때 사용하는 prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        log.info("Configuring message broker");

        // 메시지 브로커가 /topic으로 시작하는 메시지를 구독자에게 전달
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 /app으로 시작하는 메시지를 서버로 전송
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 클라이언트로부터 들어오는 메시지 채널 설정
     * ChannelInterceptor를 등록하여 모든 STOMP 메시지를 로깅합니다
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        log.info("Configuring client inbound channel with interceptor");
        registration.interceptors(channelInterceptor);
    }
}
