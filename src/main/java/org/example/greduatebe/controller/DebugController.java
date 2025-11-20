package org.example.greduatebe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 디버그용 Controller
 * 데이터베이스 상태 확인용
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 데이터베이스 테이블 카운트 조회
     */
    @GetMapping("/db-status")
    public Map<String, Object> getDatabaseStatus() {
        log.info("🔍 [Debug] Database status requested");

        Map<String, Object> status = new HashMap<>();

        try {
            // mission_attempts 카운트
            Long attemptsCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mission_attempts", Long.class);
            status.put("mission_attempts_count", attemptsCount);

            // mission_events 카운트
            Long eventsCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mission_events", Long.class);
            status.put("mission_events_count", eventsCount);

            // reviews 카운트
            Long reviewsCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM reviews", Long.class);
            status.put("reviews_count", reviewsCount);

            // 최근 5개 reviews 조회
            List<Map<String, Object>> recentReviews = jdbcTemplate.queryForList(
                    "SELECT review_id, attempt_id, rating, rating_text, has_feedback, submitted_at " +
                    "FROM reviews ORDER BY created_at DESC LIMIT 5");
            status.put("recent_reviews", recentReviews);

            // 최근 5개 attempts 조회
            List<Map<String, Object>> recentAttempts = jdbcTemplate.queryForList(
                    "SELECT attempt_id, mission_type, mission_name, status, start_time " +
                    "FROM mission_attempts ORDER BY start_time DESC LIMIT 5");
            status.put("recent_attempts", recentAttempts);

            status.put("status", "success");
            log.info("✅ [Debug] Database status: attempts={}, events={}, reviews={}",
                    attemptsCount, eventsCount, reviewsCount);

        } catch (Exception e) {
            log.error("❌ [Debug] Error getting database status", e);
            status.put("status", "error");
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * Reviews 테이블 상세 조회
     */
    @GetMapping("/reviews")
    public Map<String, Object> getReviewsDetail() {
        log.info("🔍 [Debug] Reviews detail requested");

        Map<String, Object> result = new HashMap<>();

        try {
            // 전체 reviews 조회
            List<Map<String, Object>> allReviews = jdbcTemplate.queryForList(
                    "SELECT r.review_id, r.attempt_id, r.rating, r.rating_text, r.feedback, " +
                    "r.has_feedback, r.submitted_at, r.created_at, " +
                    "ma.mission_type, ma.mission_name, ma.status " +
                    "FROM reviews r " +
                    "LEFT JOIN mission_attempts ma ON r.attempt_id = ma.attempt_id " +
                    "ORDER BY r.created_at DESC LIMIT 20");

            result.put("reviews", allReviews);
            result.put("count", allReviews.size());
            result.put("status", "success");

            log.info("✅ [Debug] Found {} reviews", allReviews.size());

        } catch (Exception e) {
            log.error("❌ [Debug] Error getting reviews", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 특정 attemptId의 review 조회
     */
    @GetMapping("/review-by-attempt")
    public Map<String, Object> getReviewByAttempt(String attemptId) {
        log.info("🔍 [Debug] Review by attempt requested - attemptId: {}", attemptId);

        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> reviews = jdbcTemplate.queryForList(
                    "SELECT * FROM reviews WHERE attempt_id = ?", attemptId);

            result.put("reviews", reviews);
            result.put("count", reviews.size());
            result.put("status", "success");

        } catch (Exception e) {
            log.error("❌ [Debug] Error getting review", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }
}
