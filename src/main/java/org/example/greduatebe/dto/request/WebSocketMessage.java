package org.example.greduatebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket 메시지 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessage {

    @NotBlank(message = "eventType은 필수입니다")
    private String eventType;

    @NotNull(message = "timestamp는 필수입니다")
    private LocalDateTime timestamp;

    @NotBlank(message = "sessionId는 필수입니다")
    private String sessionId;

    private String attemptId;

    @NotNull(message = "data는 필수입니다")
    private Map<String, Object> data;
}
