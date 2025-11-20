package org.example.greduatebe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 미션 상세 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionDetailResponse {

    private String attemptId;
    private String sessionId;
    private MissionType missionType;
    private String missionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalDuration;
    private MissionStatus status;
    private Long eventCount;
    private List<EventInfo> events;
    private ReviewInfo review;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventInfo {
        private String eventId;
        private String eventType;
        private LocalDateTime timestamp;
        private Map<String, Object> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewInfo {
        private String reviewId;
        private Integer rating;
        private String ratingText;
        private String feedback;
        private Boolean hasFeedback;
        private LocalDateTime submittedAt;
    }
}
