package org.example.greduatebe.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 미션 시도 상세 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptDetailDto {

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
     * 총 소요 시간 (초)
     */
    private BigDecimal totalDuration;

    /**
     * 총 소요 시간 (포맷된 문자열)
     */
    private String durationFormatted;

    /**
     * 최종 상태
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
     * 피드백 (nullable)
     */
    private String feedback;

    /**
     * 타임라인 이벤트 목록
     */
    private List<TimelineEventDto> timelineEvents;

    /**
     * 단계별 상세 (포트폴리오 미션만 해당)
     */
    private List<StepDetailDto> stepDetails;

    /**
     * 타임라인 이벤트 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEventDto {
        /**
         * 이벤트 타입
         */
        private String eventType;

        /**
         * 타임스탬프
         */
        private LocalDateTime timestamp;

        /**
         * 페이지 경로 (page_view 이벤트의 경우)
         */
        private String page;

        /**
         * 이전 이벤트와의 시간 차이 (초)
         */
        private BigDecimal timeDiff;

        /**
         * 데이터 미리보기 (JSON 요약)
         */
        private String dataPreview;

        /**
         * 원본 데이터
         */
        private Map<String, Object> data;

        /**
         * 미션과 관련된 페이지 여부 (page_view 이벤트의 경우)
         */
        private Boolean isMissionRelevant;

        /**
         * 페이지 체류 시간 (밀리초)
         */
        private Long duration;

        /**
         * 스크롤 깊이 (%)
         */
        private Integer scrollDepth;

        /**
         * 이전 페이지 경로 (referrer)
         */
        private String referrer;
    }
}
