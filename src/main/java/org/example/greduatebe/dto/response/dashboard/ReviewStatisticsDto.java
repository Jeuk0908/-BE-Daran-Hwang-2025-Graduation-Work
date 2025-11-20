package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 후기 통계 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsDto {

    /**
     * 총 후기 개수
     */
    private Long totalReviews;

    /**
     * 5점 개수
     */
    private Long rating5Count;

    /**
     * 4점 개수
     */
    private Long rating4Count;

    /**
     * 3점 개수
     */
    private Long rating3Count;

    /**
     * 2점 개수
     */
    private Long rating2Count;

    /**
     * 1점 개수
     */
    private Long rating1Count;

    /**
     * 텍스트 후기가 있는 개수
     */
    private Long withFeedbackCount;

    /**
     * 평균 평점
     */
    private java.math.BigDecimal avgRating;
}
