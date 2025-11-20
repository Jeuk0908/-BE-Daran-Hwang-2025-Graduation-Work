package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 포트폴리오 미션 단계별 상세 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepDetailDto {

    /**
     * 단계 번호 (1-5)
     */
    private Integer step;

    /**
     * 단계 이름 (예: "risk_type_selection")
     */
    private String stepName;

    /**
     * 선택한 값 (예: "공격형", "글로벌 탐험가")
     */
    private String selectedLabel;

    /**
     * 소요 시간 (초)
     */
    private BigDecimal timeOnStep;

    /**
     * 평균 대비 느린지 여부
     */
    private Boolean isSlow;

    /**
     * 추가 데이터 (예: 비중 조정 횟수, 이름 길이 등)
     */
    private String additionalInfo;
}
