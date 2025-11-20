package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 시간대별 분포 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyDistributionDto {

    /**
     * 시간대 범위 시작 (0, 3, 6, 9, 12, 15, 18, 21)
     */
    private Integer hourRangeStart;

    /**
     * 시간대 레이블 (예: "00:00-03:00")
     */
    private String hourRange;

    /**
     * 총 시도 건수
     */
    private Long totalAttempts;

    /**
     * 완료 건수
     */
    private Long completedCount;

    /**
     * 완료율 (%)
     */
    private BigDecimal completionRate;
}
