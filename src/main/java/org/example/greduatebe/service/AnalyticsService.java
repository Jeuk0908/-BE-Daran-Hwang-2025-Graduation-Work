package org.example.greduatebe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;
import org.example.greduatebe.repository.MissionAttemptRepository;
import org.example.greduatebe.repository.ReviewRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 분석 관련 비즈니스 로직 처리 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final MissionAttemptRepository missionAttemptRepository;
    private final ReviewRepository reviewRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 미션 분석 데이터 조회
     * @param missionType 미션 타입
     * @return 분석 데이터
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMissionAnalytics(MissionType missionType) {
        log.info("Getting mission analytics - missionType: {}", missionType);

        Map<String, Object> analytics = new HashMap<>();

        // 전체 미션 시도 수
        Long totalAttempts = countTotalAttempts(missionType);
        analytics.put("totalAttempts", totalAttempts);

        // 완료된 미션 수
        Long completedAttempts = countCompletedAttempts(missionType);
        analytics.put("completedAttempts", completedAttempts);

        // 완료율 계산
        BigDecimal completionRate = calculateCompletionRate(totalAttempts, completedAttempts);
        analytics.put("completionRate", completionRate);

        // 평균 소요 시간 (초)
        BigDecimal avgDuration = calculateAverageDuration(missionType);
        analytics.put("avgDuration", avgDuration);

        // 평균 평점
        BigDecimal avgRating = calculateAverageRating(missionType);
        analytics.put("avgRating", avgRating);

        // 리뷰 제출 수
        Long reviewCount = countReviews(missionType);
        analytics.put("reviewCount", reviewCount);

        log.info("Analytics calculated - totalAttempts: {}, completionRate: {}%, avgDuration: {}s, avgRating: {}",
                totalAttempts, completionRate, avgDuration, avgRating);

        return analytics;
    }

    /**
     * 전체 미션 시도 수 조회
     */
    private Long countTotalAttempts(MissionType missionType) {
        String sql = "SELECT COUNT(*) FROM mission_attempts WHERE mission_type = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, missionType.name());
    }

    /**
     * 완료된 미션 수 조회
     */
    private Long countCompletedAttempts(MissionType missionType) {
        String sql = "SELECT COUNT(*) FROM mission_attempts WHERE mission_type = ? AND status = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, missionType.name(), MissionStatus.COMPLETED.name());
    }

    /**
     * 완료율 계산
     */
    private BigDecimal calculateCompletionRate(Long totalAttempts, Long completedAttempts) {
        if (totalAttempts == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completedAttempts)
                .divide(BigDecimal.valueOf(totalAttempts), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 평균 소요 시간 계산
     */
    private BigDecimal calculateAverageDuration(MissionType missionType) {
        String sql = "SELECT AVG(total_duration) FROM mission_attempts " +
                     "WHERE mission_type = ? AND status = ? AND total_duration IS NOT NULL";
        BigDecimal avgDuration = jdbcTemplate.queryForObject(sql, BigDecimal.class,
                missionType.name(), MissionStatus.COMPLETED.name());

        if (avgDuration == null) {
            return BigDecimal.ZERO;
        }
        return avgDuration.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 평균 평점 계산
     */
    private BigDecimal calculateAverageRating(MissionType missionType) {
        String sql = "SELECT AVG(r.rating) FROM reviews r " +
                     "JOIN mission_attempts ma ON r.attempt_id = ma.attempt_id " +
                     "WHERE ma.mission_type = ?";
        BigDecimal avgRating = jdbcTemplate.queryForObject(sql, BigDecimal.class, missionType.name());

        if (avgRating == null) {
            return BigDecimal.ZERO;
        }
        return avgRating.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 리뷰 제출 수 조회
     */
    private Long countReviews(MissionType missionType) {
        String sql = "SELECT COUNT(r.*) FROM reviews r " +
                     "JOIN mission_attempts ma ON r.attempt_id = ma.attempt_id " +
                     "WHERE ma.mission_type = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, missionType.name());
    }
}
