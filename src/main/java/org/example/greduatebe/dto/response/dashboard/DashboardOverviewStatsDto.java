package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 대시보드 전체 통계 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewStatsDto {

    /**
     * 총 시도 건수
     */
    private Long totalAttempts;

    /**
     * 전체 완료율 (%)
     */
    private BigDecimal overallCompletionRate;

    /**
     * 평균 완료 시간 (초)
     */
    private BigDecimal avgCompletionTime;

    /**
     * 평균 완료 시간 (포맷된 문자열, 예: "6분 45초")
     */
    private String avgCompletionTimeFormatted;

    /**
     * 평균 평점 (1-5)
     */
    private BigDecimal avgRating;

    /**
     * 완료된 시도 건수
     */
    private Long completedAttempts;

    /**
     * 포기한 시도 건수
     */
    private Long quittedAttempts;
}
