package org.example.greduatebe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 Interceptor
 * 연결 시도 단계부터 상세하게 로깅합니다
 */
@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        String uri = request.getURI().toString();
        String remoteAddress = request.getRemoteAddress() != null ?
                request.getRemoteAddress().toString() : "unknown";
        String origin = request.getHeaders().getOrigin();

        log.info("🔵 [WebSocket] Handshake request received");
        log.info("   - URI: {}", uri);
        log.info("   - Remote Address: {}", remoteAddress);
        log.info("   - Origin: {}", origin);
        log.info("   - Headers: {}", request.getHeaders());

        // 핸드셰이크 허용
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {

        if (exception != null) {
            log.error("❌ [WebSocket] Handshake failed - error: {}", exception.getMessage(), exception);
        } else {
            log.info("✅ [WebSocket] Handshake completed successfully");
            log.info("   - URI: {}", request.getURI());
        }
    }
}
