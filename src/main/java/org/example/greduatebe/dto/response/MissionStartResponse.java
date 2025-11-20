package org.example.greduatebe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 미션 시작 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionStartResponse {

    private String attemptId;
    private String wsUrl;
    private Long expiresIn;
}
