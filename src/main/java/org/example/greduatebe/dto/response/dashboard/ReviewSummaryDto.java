package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.greduatebe.entity.MissionType;

import java.time.LocalDateTime;

/**
 * 후기 요약 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {

    /**
     * Review ID
     */
    private String reviewId;

    /**
     * Attempt ID
     */
    private String attemptId;

    /**
     * 미션 타입
     */
    private MissionType missionType;

    /**
     * 미션 이름
     */
    private String missionName;

    /**
     * 평점 (1-5)
     */
    private Integer rating;

    /**
     * 평점 텍스트 (예: "매우 쉬움", "적당함")
     */
    private String ratingText;

    /**
     * 후기 내용
     */
    private String feedback;

    /**
     * 후기 존재 여부
     */
    private Boolean hasFeedback;

    /**
     * 제출 시간
     */
    private LocalDateTime submittedAt;

    /**
     * 미션 소요 시간 (초)
     */
    private java.math.BigDecimal missionDuration;

    /**
     * 미션 소요 시간 (포맷된 문자열)
     */
    private String missionDurationFormatted;

    /**
     * 별 아이콘 문자열 (예: "⭐⭐⭐⭐⭐")
     */
    public String getStarRating() {
        if (rating == null) {
            return "";
        }
        return "⭐".repeat(rating);
    }
}
