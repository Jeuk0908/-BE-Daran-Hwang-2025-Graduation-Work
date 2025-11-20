package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.greduatebe.entity.MissionType;

import java.math.BigDecimal;

/**
 * 미션별 완료율 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionCompletionRateDto {

    /**
     * 미션 타입
     */
    private MissionType missionType;

    /**
     * 총 시도 건수
     */
    private Long totalAttempts;

    /**
     * 완료 건수
     */
    private Long completedCount;

    /**
     * 포기 건수
     */
    private Long quittedCount;

    /**
     * 완료율 (%)
     */
    private BigDecimal completionRate;

    /**
     * 평균 완료 시간 (초)
     */
    private BigDecimal avgDuration;

    /**
     * 평균 완료 시간 (포맷된 문자열)
     */
    private String avgDurationFormatted;
}
