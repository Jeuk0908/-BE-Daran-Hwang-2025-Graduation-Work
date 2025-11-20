package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 최근 미션 시도 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentAttemptDto {

    /**
     * Attempt ID
     */
    private String attemptId;

    /**
     * Session ID
     */
    private String sessionId;

    /**
     * 미션 타입
     */
    private MissionType missionType;

    /**
     * 미션 이름
     */
    private String missionName;

    /**
     * 시작 시간
     */
    private LocalDateTime startTime;

    /**
     * 종료 시간
     */
    private LocalDateTime endTime;

    /**
     * 소요 시간 (초)
     */
    private BigDecimal duration;

    /**
     * 소요 시간 (포맷된 문자열)
     */
    private String durationFormatted;

    /**
     * 상태
     */
    private MissionStatus status;

    /**
     * 평점 (nullable)
     */
    private Integer rating;

    /**
     * 평점 텍스트 (nullable)
     */
    private String ratingText;

    /**
     * 이벤트 개수
     */
    private Long eventCount;
}
