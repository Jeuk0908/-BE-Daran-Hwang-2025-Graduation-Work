package org.example.greduatebe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greduatebe.entity.MissionType;

import java.time.LocalDateTime;

/**
 * 미션 시작 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionStartRequest {

    @NotBlank(message = "sessionId는 필수입니다")
    private String sessionId;

    @NotNull(message = "missionType은 필수입니다")
    private MissionType missionType;

    @NotNull(message = "timestamp는 필수입니다")
    private LocalDateTime timestamp;
}
