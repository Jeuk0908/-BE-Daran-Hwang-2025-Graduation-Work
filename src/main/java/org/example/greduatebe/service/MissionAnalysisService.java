package org.example.greduatebe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.response.dashboard.*;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;
import org.example.greduatebe.repository.MissionAttemptRepository;
import org.example.greduatebe.repository.MissionEventRepository;
import org.example.greduatebe.repository.ReviewRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 대시보드 분석 Service
 * DASHBOARD_GUIDE.md 스펙에 따라 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MissionAnalysisService {

    private final MissionAttemptRepository missionAttemptRepository;
    private final MissionEventRepository missionEventRepository;
    private final ReviewRepository reviewRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 대시보드 전체 통계 조회
     * DASHBOARD_GUIDE.md Section 3.2 참고
     *
     * @return 전체 통계 DTO
     */
    @Transactional(readOnly = true)
    public DashboardOverviewStatsDto getOverviewStats() {
        log.info("Getting dashboard overview stats");

        String sql = """
            SELECT
                COUNT(*) as total_attempts,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
                COUNT(CASE WHEN status = 'QUITTED' THEN 1 END) as quitted,
                AVG(CASE WHEN status = 'COMPLETED' THEN total_duration END) as avg_duration,
                ROUND(COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)::numeric / NULLIF(COUNT(*), 0) * 100, 2) as completion_rate
            FROM mission_attempts
            WHERE status IN ('COMPLETED', 'QUITTED')
            """;

        Map<String, Object> result = jdbcTemplate.queryForMap(sql);

        // 평균 평점 조회
        String ratingSQL = "SELECT ROUND(AVG(r.rating), 2) as avg_rating FROM reviews r";
        BigDecimal avgRating = jdbcTemplate.queryForObject(ratingSQL, BigDecimal.class);

        Long totalAttempts = ((Number) result.get("total_attempts")).longValue();
        Long completed = ((Number) result.get("completed")).longValue();
        Long quitted = ((Number) result.get("quitted")).longValue();
        BigDecimal avgDuration = (BigDecimal) result.get("avg_duration");
        BigDecimal completionRate = (BigDecimal) result.get("completion_rate");

        return DashboardOverviewStatsDto.builder()
                .totalAttempts(totalAttempts)
                .completedAttempts(completed)
                .quittedAttempts(quitted)
                .overallCompletionRate(completionRate != null ? completionRate : BigDecimal.ZERO)
                .avgCompletionTime(avgDuration != null ? avgDuration : BigDecimal.ZERO)
                .avgCompletionTimeFormatted(formatDuration(avgDuration))
                .avgRating(avgRating != null ? avgRating : BigDecimal.ZERO)
                .build();
    }

    /**
     * 미션별 완료율 조회
     * DASHBOARD_GUIDE.md Section 3.2 참고
     *
     * @return 미션별 완료율 리스트
     */
    @Transactional(readOnly = true)
    public List<MissionCompletionRateDto> getCompletionRates() {
        log.info("Getting mission completion rates");

        String sql = """
            SELECT
                mission_type,
                COUNT(*) as total_attempts,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
                COUNT(CASE WHEN status = 'QUITTED' THEN 1 END) as quitted,
                ROUND(COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)::numeric / NULLIF(COUNT(*), 0) * 100, 2) as completion_rate,
                AVG(total_duration) as avg_duration
            FROM mission_attempts
            WHERE status IN ('COMPLETED', 'QUITTED')
            GROUP BY mission_type
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MissionType missionType = MissionType.valueOf(rs.getString("mission_type"));
            BigDecimal avgDuration = rs.getBigDecimal("avg_duration");

            return MissionCompletionRateDto.builder()
                    .missionType(missionType)
                    .totalAttempts(rs.getLong("total_attempts"))
                    .completedCount(rs.getLong("completed"))
                    .quittedCount(rs.getLong("quitted"))
                    .completionRate(rs.getBigDecimal("completion_rate"))
                    .avgDuration(avgDuration)
                    .avgDurationFormatted(formatDuration(avgDuration))
                    .build();
        });
    }

    /**
     * 최근 미션 시도 조회
     * DASHBOARD_GUIDE.md Section 4.1 참고
     *
     * @param limit 조회할 개수
     * @return 최근 미션 시도 리스트
     */
    @Transactional(readOnly = true)
    public List<RecentAttemptDto> getRecentAttempts(int limit) {
        log.info("Getting recent attempts - limit: {}", limit);

        String sql = """
            SELECT
                ma.attempt_id,
                ma.session_id,
                ma.mission_type,
                ma.mission_name,
                ma.start_time,
                ma.end_time,
                ma.total_duration,
                ma.status,
                r.rating,
                r.rating_text,
                (SELECT COUNT(*) FROM mission_events me WHERE me.attempt_id = ma.attempt_id) as event_count
            FROM mission_attempts ma
            LEFT JOIN reviews r ON r.attempt_id = ma.attempt_id
            ORDER BY ma.start_time DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BigDecimal duration = rs.getBigDecimal("total_duration");

            return RecentAttemptDto.builder()
                    .attemptId(rs.getString("attempt_id"))
                    .sessionId(rs.getString("session_id"))
                    .missionType(MissionType.valueOf(rs.getString("mission_type")))
                    .missionName(rs.getString("mission_name"))
                    .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                    .endTime(rs.getTimestamp("end_time") != null ?
                            rs.getTimestamp("end_time").toLocalDateTime() : null)
                    .duration(duration)
                    .durationFormatted(formatDuration(duration))
                    .status(MissionStatus.valueOf(rs.getString("status")))
                    .rating(rs.getObject("rating") != null ? rs.getInt("rating") : null)
                    .ratingText(rs.getString("rating_text"))
                    .eventCount(rs.getLong("event_count"))
                    .build();
        }, limit);
    }

    /**
     * 시간대별 분포 조회
     * DASHBOARD_GUIDE.md Section 3.2 참고
     *
     * @return 시간대별 분포 리스트
     */
    @Transactional(readOnly = true)
    public List<HourlyDistributionDto> getHourlyDistribution() {
        log.info("Getting hourly distribution");

        String sql = """
            SELECT
                FLOOR(EXTRACT(HOUR FROM start_time) / 3) * 3 as hour_range_start,
                COUNT(*) as total_attempts,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
                ROUND(COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)::numeric / NULLIF(COUNT(*), 0) * 100, 2) as completion_rate
            FROM mission_attempts
            GROUP BY hour_range_start
            ORDER BY hour_range_start
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int hourStart = rs.getInt("hour_range_start");
            return HourlyDistributionDto.builder()
                    .hourRangeStart(hourStart)
                    .hourRange(String.format("%02d:00-%02d:00", hourStart, hourStart + 3))
                    .totalAttempts(rs.getLong("total_attempts"))
                    .completedCount(rs.getLong("completed"))
                    .completionRate(rs.getBigDecimal("completion_rate"))
                    .build();
        });
    }

    /**
     * 미션 시도 상세 조회
     * DASHBOARD_GUIDE.md Section 3.1, 4.2 참고
     *
     * @param attemptId Attempt ID
     * @return 미션 시도 상세 DTO
     */
    @Transactional(readOnly = true)
    public AttemptDetailDto getAttemptDetail(String attemptId) {
        log.info("Getting attempt detail - attemptId: {}", attemptId);

        // 기본 정보 조회
        String attemptSQL = """
            SELECT
                ma.attempt_id,
                ma.session_id,
                ma.mission_type,
                ma.mission_name,
                ma.start_time,
                ma.end_time,
                ma.total_duration,
                ma.status,
                r.rating,
                r.rating_text,
                r.feedback
            FROM mission_attempts ma
            LEFT JOIN reviews r ON r.attempt_id = ma.attempt_id
            WHERE ma.attempt_id = ?
            """;

        AttemptDetailDto.AttemptDetailDtoBuilder builder = jdbcTemplate.queryForObject(attemptSQL,
                (rs, rowNum) -> {
                    BigDecimal duration = rs.getBigDecimal("total_duration");
                    return AttemptDetailDto.builder()
                            .attemptId(rs.getString("attempt_id"))
                            .sessionId(rs.getString("session_id"))
                            .missionType(MissionType.valueOf(rs.getString("mission_type")))
                            .missionName(rs.getString("mission_name"))
                            .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                            .endTime(rs.getTimestamp("end_time") != null ?
                                    rs.getTimestamp("end_time").toLocalDateTime() : null)
                            .totalDuration(duration)
                            .durationFormatted(formatDuration(duration))
                            .status(MissionStatus.valueOf(rs.getString("status")))
                            .rating(rs.getObject("rating") != null ? rs.getInt("rating") : null)
                            .ratingText(rs.getString("rating_text"))
                            .feedback(rs.getString("feedback"));
                }, attemptId);

        if (builder == null) {
            log.warn("Attempt not found - attemptId: {}", attemptId);
            return null;
        }

        // 타임라인 이벤트 조회
        List<AttemptDetailDto.TimelineEventDto> timeline = getTimelineEvents(attemptId);
        builder.timelineEvents(timeline);

        // 포트폴리오 미션인 경우 단계별 상세 조회
        AttemptDetailDto attempt = builder.build();
        if (attempt.getMissionType() == MissionType.PORTFOLIO) {
            List<StepDetailDto> stepDetails = getPortfolioStepDetails(attemptId);
            builder.stepDetails(stepDetails);
        }

        return builder.build();
    }

    /**
     * 타임라인 이벤트 조회
     * DASHBOARD_GUIDE.md Section 2.1 참고
     */
    private List<AttemptDetailDto.TimelineEventDto> getTimelineEvents(String attemptId) {
        String sql = """
            SELECT
                event_type,
                timestamp,
                data,
                LAG(timestamp) OVER (ORDER BY timestamp) as prev_timestamp
            FROM mission_events
            WHERE attempt_id = ?
            ORDER BY timestamp ASC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String eventType = rs.getString("event_type");
            LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
            Timestamp prevTs = rs.getTimestamp("prev_timestamp");
            String dataJson = rs.getString("data");

            // JSON 파싱
            Map<String, Object> data = parseJsonData(dataJson);

            // 시간 차이 계산
            BigDecimal timeDiff = null;
            if (prevTs != null) {
                LocalDateTime prevTimestamp = prevTs.toLocalDateTime();
                long diffSeconds = java.time.Duration.between(prevTimestamp, timestamp).getSeconds();
                timeDiff = BigDecimal.valueOf(diffSeconds);
            }

            // 페이지 경로 추출
            String page = data != null ? (String) data.get("page") : null;

            // 데이터 미리보기 생성
            String dataPreview = createDataPreview(data);

            // 미션 관련 필드 추출 (page_view 이벤트의 경우)
            Boolean isMissionRelevant = null;
            Long duration = null;
            Integer scrollDepth = null;
            String referrer = null;

            if (data != null) {
                // isMissionRelevant 추출
                Object isMissionRelevantObj = data.get("isMissionRelevant");
                if (isMissionRelevantObj instanceof Boolean) {
                    isMissionRelevant = (Boolean) isMissionRelevantObj;
                }

                // duration 추출 (밀리초)
                Object durationObj = data.get("duration");
                if (durationObj instanceof Number) {
                    duration = ((Number) durationObj).longValue();
                }

                // scrollDepth 추출 (%)
                Object scrollDepthObj = data.get("scrollDepth");
                if (scrollDepthObj instanceof Number) {
                    scrollDepth = ((Number) scrollDepthObj).intValue();
                }

                // referrer 추출
                referrer = (String) data.get("referrer");
            }

            return AttemptDetailDto.TimelineEventDto.builder()
                    .eventType(eventType)
                    .timestamp(timestamp)
                    .page(page)
                    .timeDiff(timeDiff)
                    .dataPreview(dataPreview)
                    .data(data)
                    .isMissionRelevant(isMissionRelevant)
                    .duration(duration)
                    .scrollDepth(scrollDepth)
                    .referrer(referrer)
                    .build();
        }, attemptId);
    }

    /**
     * 포트폴리오 미션 단계별 상세 조회
     * DASHBOARD_GUIDE.md Section 2.2 참고
     */
    private List<StepDetailDto> getPortfolioStepDetails(String attemptId) {
        String sql = """
            SELECT
                data->>'step' as step,
                data->>'stepName' as step_name,
                data->>'selectedLabel' as selected_label,
                data->>'timeOnStep' as time_on_step,
                data->>'adjustmentCount' as adjustment_count,
                data->>'nameLength' as name_length,
                data->>'finalPercentage' as final_percentage,
                data
            FROM mission_events
            WHERE attempt_id = ?
              AND event_type = 'portfolio_creation_step'
            ORDER BY (data->>'step')::int
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int step = Integer.parseInt(rs.getString("step"));
            BigDecimal timeOnStep = new BigDecimal(rs.getString("time_on_step"));

            // 평균 시간 대비 느린지 판단 (평균의 2배 초과)
            boolean isSlow = isStepSlow(step, timeOnStep);

            // 단계별 추가 정보
            String additionalInfo = buildAdditionalInfo(step, rs);

            return StepDetailDto.builder()
                    .step(step)
                    .stepName(rs.getString("step_name"))
                    .selectedLabel(rs.getString("selected_label"))
                    .timeOnStep(timeOnStep)
                    .isSlow(isSlow)
                    .additionalInfo(additionalInfo)
                    .build();
        }, attemptId);
    }

    /**
     * 단계가 평균 대비 느린지 판단
     */
    private boolean isStepSlow(int step, BigDecimal timeOnStep) {
        String sql = """
            SELECT AVG((data->>'timeOnStep')::numeric) as avg_time
            FROM mission_events
            WHERE event_type = 'portfolio_creation_step'
              AND data->>'step' = ?
            """;

        BigDecimal avgTime = jdbcTemplate.queryForObject(sql, BigDecimal.class, String.valueOf(step));
        if (avgTime == null) {
            return false;
        }

        return timeOnStep.compareTo(avgTime.multiply(BigDecimal.valueOf(2))) > 0;
    }

    /**
     * 단계별 추가 정보 생성
     */
    private String buildAdditionalInfo(int step, java.sql.ResultSet rs) throws java.sql.SQLException {
        return switch (step) {
            case 3 -> {
                String adjustmentCount = rs.getString("adjustment_count");
                yield adjustmentCount != null ? "비중 조정 " + adjustmentCount + "회" : null;
            }
            case 4 -> {
                String nameLength = rs.getString("name_length");
                yield nameLength != null ? "이름 길이: " + nameLength + "자" : null;
            }
            case 5 -> {
                String finalPercentage = rs.getString("final_percentage");
                yield finalPercentage != null ? "목표 수익률: " + finalPercentage + "%" : null;
            }
            default -> null;
        };
    }

    /**
     * JSON 데이터 파싱
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonData(String jsonData) {
        if (jsonData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonData, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON data: {}", jsonData, e);
            return null;
        }
    }

    /**
     * 데이터 미리보기 생성
     */
    private String createDataPreview(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        List<String> preview = new ArrayList<>();
        data.forEach((key, value) -> {
            if (!"page".equals(key) && value != null) {
                preview.add(key + ": " + value);
            }
        });

        if (preview.isEmpty()) {
            return null;
        }

        String result = String.join(", ", preview);
        return result.length() > 100 ? result.substring(0, 100) + "..." : result;
    }

    /**
     * Duration을 포맷된 문자열로 변환 (예: "6분 45초", "18초")
     */
    private String formatDuration(BigDecimal duration) {
        if (duration == null || duration.compareTo(BigDecimal.ZERO) == 0) {
            return "-";
        }

        long seconds = duration.longValue();
        if (seconds < 60) {
            return seconds + "초";
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (remainingSeconds == 0) {
            return minutes + "분";
        }

        return minutes + "분 " + remainingSeconds + "초";
    }

    /**
     * 최근 후기 조회
     *
     * @param limit 조회할 개수
     * @return 최근 후기 리스트
     */
    @Transactional(readOnly = true)
    public List<ReviewSummaryDto> getRecentReviews(int limit) {
        log.info("Getting recent reviews - limit: {}", limit);

        String sql = """
            SELECT
                r.review_id,
                r.attempt_id,
                r.rating,
                r.rating_text,
                r.feedback,
                r.has_feedback,
                r.submitted_at,
                ma.mission_type,
                ma.mission_name,
                ma.total_duration
            FROM reviews r
            JOIN mission_attempts ma ON r.attempt_id = ma.attempt_id
            ORDER BY r.submitted_at DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BigDecimal duration = rs.getBigDecimal("total_duration");

            return ReviewSummaryDto.builder()
                    .reviewId(rs.getString("review_id"))
                    .attemptId(rs.getString("attempt_id"))
                    .missionType(MissionType.valueOf(rs.getString("mission_type")))
                    .missionName(rs.getString("mission_name"))
                    .rating(rs.getObject("rating") != null ? rs.getInt("rating") : null)  // null 처리
                    .ratingText(rs.getString("rating_text"))
                    .feedback(rs.getString("feedback"))
                    .hasFeedback(rs.getBoolean("has_feedback"))
                    .submittedAt(rs.getTimestamp("submitted_at") != null ?
                            rs.getTimestamp("submitted_at").toLocalDateTime() : null)
                    .missionDuration(duration)
                    .missionDurationFormatted(formatDuration(duration))
                    .build();
        }, limit);
    }

    /**
     * 전체 후기 조회 (필터링 지원)
     *
     * @param ratingFilter 평점 필터 (null이면 전체)
     * @param missionTypeFilter 미션 타입 필터 (null이면 전체)
     * @param hasFeedbackFilter 후기 유무 필터 (null이면 전체)
     * @return 후기 리스트
     */
    @Transactional(readOnly = true)
    public List<ReviewSummaryDto> getAllReviews(String ratingFilter, String missionTypeFilter, Boolean hasFeedbackFilter) {
        log.info("Getting all reviews - ratingFilter: {}, missionTypeFilter: {}, hasFeedbackFilter: {}",
                ratingFilter, missionTypeFilter, hasFeedbackFilter);

        StringBuilder sql = new StringBuilder("""
            SELECT
                r.review_id,
                r.attempt_id,
                r.rating,
                r.rating_text,
                r.feedback,
                r.has_feedback,
                r.submitted_at,
                ma.mission_type,
                ma.mission_name,
                ma.total_duration
            FROM reviews r
            JOIN mission_attempts ma ON r.attempt_id = ma.attempt_id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        // 평점 필터
        if (ratingFilter != null && !ratingFilter.isEmpty()) {
            sql.append(" AND r.rating = ?");
            params.add(Integer.parseInt(ratingFilter));
        }

        // 미션 타입 필터
        if (missionTypeFilter != null && !missionTypeFilter.isEmpty()) {
            sql.append(" AND ma.mission_type = ?");
            params.add(missionTypeFilter);
        }

        // 후기 유무 필터
        if (hasFeedbackFilter != null && hasFeedbackFilter) {
            sql.append(" AND r.has_feedback = true");
        }

        sql.append(" ORDER BY r.submitted_at DESC");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            BigDecimal duration = rs.getBigDecimal("total_duration");

            return ReviewSummaryDto.builder()
                    .reviewId(rs.getString("review_id"))
                    .attemptId(rs.getString("attempt_id"))
                    .missionType(MissionType.valueOf(rs.getString("mission_type")))
                    .missionName(rs.getString("mission_name"))
                    .rating(rs.getObject("rating") != null ? rs.getInt("rating") : null)  // null 처리
                    .ratingText(rs.getString("rating_text"))
                    .feedback(rs.getString("feedback"))
                    .hasFeedback(rs.getBoolean("has_feedback"))
                    .submittedAt(rs.getTimestamp("submitted_at") != null ?
                            rs.getTimestamp("submitted_at").toLocalDateTime() : null)
                    .missionDuration(duration)
                    .missionDurationFormatted(formatDuration(duration))
                    .build();
        }, params.toArray());
    }

    /**
     * 후기 통계 조회
     *
     * @return 후기 통계 DTO
     */
    @Transactional(readOnly = true)
    public ReviewStatisticsDto getReviewStatistics() {
        log.info("Getting review statistics");

        String sql = """
            SELECT
                COUNT(*) as total_reviews,
                COUNT(CASE WHEN rating = 5 THEN 1 END) as rating_5,
                COUNT(CASE WHEN rating = 4 THEN 1 END) as rating_4,
                COUNT(CASE WHEN rating = 3 THEN 1 END) as rating_3,
                COUNT(CASE WHEN rating = 2 THEN 1 END) as rating_2,
                COUNT(CASE WHEN rating = 1 THEN 1 END) as rating_1,
                COUNT(CASE WHEN has_feedback = true THEN 1 END) as with_feedback,
                ROUND(AVG(rating), 2) as avg_rating
            FROM reviews
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                ReviewStatisticsDto.builder()
                        .totalReviews(rs.getLong("total_reviews"))
                        .rating5Count(rs.getLong("rating_5"))
                        .rating4Count(rs.getLong("rating_4"))
                        .rating3Count(rs.getLong("rating_3"))
                        .rating2Count(rs.getLong("rating_2"))
                        .rating1Count(rs.getLong("rating_1"))
                        .withFeedbackCount(rs.getLong("with_feedback"))
                        .avgRating(rs.getBigDecimal("avg_rating"))
                        .build()
        );
    }
}
