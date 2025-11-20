package org.example.greduatebe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.request.WebSocketMessage;
import org.example.greduatebe.entity.MissionAttempt;
import org.example.greduatebe.entity.MissionEvent;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.Review;
import org.example.greduatebe.exception.InvalidEventException;
import org.example.greduatebe.repository.MissionAttemptRepository;
import org.example.greduatebe.repository.MissionEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 이벤트 관련 비즈니스 로직 처리 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final MissionEventRepository missionEventRepository;
    private final MissionAttemptRepository missionAttemptRepository;
    private final MissionService missionService;
    private final ReviewService reviewService;

    /**
     * 이벤트 처리
     * @param message WebSocket 메시지
     * @return 저장된 MissionEvent
     */
    @Transactional
    public MissionEvent processEvent(WebSocketMessage message) {
        long startTime = System.currentTimeMillis();

        log.info("Processing event - eventType: {}, attemptId: {}, sessionId: {}",
                message.getEventType(), message.getAttemptId(), message.getSessionId());

        // attemptId 유효성 검증
        if (message.getAttemptId() == null || message.getAttemptId().isEmpty()) {
            throw new InvalidEventException("attemptId is required");
        }

        // MissionAttempt 존재 여부 확인
        MissionAttempt missionAttempt = missionService.getMissionAttempt(message.getAttemptId());

        // timestamp가 없으면 현재 시간 사용
        LocalDateTime timestamp = message.getTimestamp() != null ?
                message.getTimestamp() : LocalDateTime.now();

        // data가 없으면 빈 Map 사용
        Map<String, Object> data = message.getData() != null ?
                message.getData() : new java.util.HashMap<>();

        // eventId 생성
        String eventId = generateEventId();

        // MissionEvent 엔티티 생성 및 저장
        MissionEvent event = MissionEvent.builder()
                .eventId(eventId)
                .attemptId(message.getAttemptId())
                .sessionId(message.getSessionId())
                .eventType(message.getEventType())
                .timestamp(timestamp)
                .data(data)
                .processingTime(System.currentTimeMillis() - startTime)
                .build();

        MissionEvent savedEvent = missionEventRepository.save(event);

        // 미션 종료 이벤트 처리
        if (isMissionEndEvent(message.getEventType())) {
            handleMissionEnd(message.getAttemptId(), message.getEventType(), timestamp);
        }

        // 미션 평가 이벤트 처리 (리뷰 저장)
        if ("mission_rating_submitted".equals(message.getEventType())) {
            handleMissionRating(message.getAttemptId(), data);
        }

        // 미션 포기 이벤트 처리 (포기 사유 저장)
        if ("mission_quitted".equals(message.getEventType())) {
            handleMissionQuit(message.getAttemptId(), data);
        }

        log.info("Event processed successfully - eventId: {}, processingTime: {}ms",
                eventId, savedEvent.getProcessingTime());

        return savedEvent;
    }

    /**
     * 미션 종료 처리
     * @param attemptId 미션 시도 ID
     * @param eventType 이벤트 타입
     * @param endTime 종료 시간
     */
    @Transactional
    public void handleMissionEnd(String attemptId, String eventType, LocalDateTime endTime) {
        log.info("Handling mission end - attemptId: {}, eventType: {}", attemptId, eventType);

        MissionAttempt missionAttempt = missionService.getMissionAttempt(attemptId);

        // 종료 시간 설정
        missionAttempt.setEndTime(endTime);

        // totalDuration 계산 (초 단위, 소수점 3자리)
        Duration duration = Duration.between(missionAttempt.getStartTime(), endTime);
        BigDecimal totalDuration = BigDecimal.valueOf(duration.toMillis() / 1000.0);
        missionAttempt.setTotalDuration(totalDuration);

        // 상태 업데이트
        MissionStatus newStatus = switch (eventType) {
            case "mission_completed" -> MissionStatus.COMPLETED;
            case "mission_quitted" -> MissionStatus.QUITTED;
            default -> MissionStatus.EXPIRED;
        };
        missionAttempt.setStatus(newStatus);

        missionAttemptRepository.save(missionAttempt);

        log.info("Mission ended - attemptId: {}, status: {}, duration: {}s",
                attemptId, newStatus, totalDuration);
    }

    /**
     * attemptId로 이벤트 목록 조회
     * @param attemptId 미션 시도 ID
     * @return MissionEvent 목록
     */
    @Transactional(readOnly = true)
    public List<MissionEvent> getEventsByAttemptId(String attemptId) {
        log.debug("Getting events by attemptId: {}", attemptId);
        return missionEventRepository.findByAttemptIdOrderByTimestampAsc(attemptId);
    }

    /**
     * attemptId로 이벤트 개수 조회
     * @param attemptId 미션 시도 ID
     * @return 이벤트 개수
     */
    @Transactional(readOnly = true)
    public Long getEventCountByAttemptId(String attemptId) {
        return missionEventRepository.countByAttemptId(attemptId);
    }

    /**
     * eventId 생성
     * @return eventId
     */
    private String generateEventId() {
        return "event_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 미션 종료 이벤트 여부 확인
     * @param eventType 이벤트 타입
     * @return 미션 종료 이벤트 여부
     */
    private boolean isMissionEndEvent(String eventType) {
        return eventType.equals("mission_completed") ||
               eventType.equals("mission_quitted") ||
               eventType.equals("mission_expired");
    }

    /**
     * 미션 평가 이벤트 처리 (리뷰 저장)
     * @param attemptId 미션 시도 ID
     * @param data 이벤트 데이터
     */
    @Transactional
    public void handleMissionRating(String attemptId, Map<String, Object> data) {
        log.info("🌟 [Review] Handling mission rating - attemptId: {}", attemptId);

        try {
            // 이미 리뷰가 있는지 확인
            if (reviewService.hasReview(attemptId)) {
                log.warn("⚠️ [Review] Review already exists for attemptId: {}", attemptId);
                return;
            }

            // 데이터 추출
            Integer rating = null;
            if (data.get("rating") instanceof Integer) {
                rating = (Integer) data.get("rating");
            } else if (data.get("rating") instanceof Number) {
                rating = ((Number) data.get("rating")).intValue();
            }

            String ratingText = (String) data.get("ratingText");
            String feedback = (String) data.get("feedback");
            Boolean hasFeedback = (Boolean) data.get("hasFeedback");

            // 필수 필드 검증
            if (rating == null || ratingText == null) {
                log.error("❌ [Review] Invalid rating data - attemptId: {}, rating: {}, ratingText: {}",
                        attemptId, rating, ratingText);
                return;
            }

            // Review 엔티티 생성
            String reviewId = "review_" + UUID.randomUUID().toString().replace("-", "");

            Review review = Review.builder()
                    .reviewId(reviewId)
                    .attemptId(attemptId)
                    .rating(rating)
                    .ratingText(ratingText)
                    .feedback(feedback)
                    .hasFeedback(hasFeedback != null ? hasFeedback : false)
                    .submittedAt(LocalDateTime.now())
                    .build();

            // 리뷰 저장
            Review savedReview = reviewService.saveReviewDirectly(review);

            log.info("✅ [Review] Review saved successfully - reviewId: {}, attemptId: {}, rating: {}",
                    savedReview.getReviewId(), attemptId, rating);

        } catch (Exception e) {
            log.error("❌ [Review] Error handling mission rating - attemptId: {}", attemptId, e);
            // 리뷰 저장 실패해도 이벤트 처리는 계속 진행
        }
    }

    /**
     * 미션 포기 이벤트 처리 (포기 사유 저장)
     * @param attemptId 미션 시도 ID
     * @param data 이벤트 데이터
     */
    @Transactional
    public void handleMissionQuit(String attemptId, Map<String, Object> data) {
        log.info("🚪 [Quit] Handling mission quit - attemptId: {}", attemptId);

        try {
            // 이미 리뷰가 있는지 확인
            if (reviewService.hasReview(attemptId)) {
                log.warn("⚠️ [Quit] Review already exists for attemptId: {}", attemptId);
                return;
            }

            // reason 필드 추출
            String reason = (String) data.get("reason");

            // reason이 없으면 리뷰를 생성하지 않음
            if (reason == null || reason.trim().isEmpty()) {
                log.info("ℹ️ [Quit] No quit reason provided for attemptId: {}", attemptId);
                return;
            }

            // Review 엔티티 생성 (포기 사유)
            String reviewId = "review_" + UUID.randomUUID().toString().replace("-", "");

            Review review = Review.builder()
                    .reviewId(reviewId)
                    .attemptId(attemptId)
                    .rating(null)  // 포기한 경우 평점 없음
                    .ratingText("포기")  // 포기 표시
                    .feedback(reason)  // 포기 사유를 feedback에 저장
                    .hasFeedback(true)
                    .submittedAt(LocalDateTime.now())
                    .build();

            // 리뷰 저장
            Review savedReview = reviewService.saveReviewDirectly(review);

            log.info("✅ [Quit] Quit reason saved as review - reviewId: {}, attemptId: {}, reason length: {}",
                    savedReview.getReviewId(), attemptId, reason.length());

        } catch (Exception e) {
            log.error("❌ [Quit] Error handling mission quit - attemptId: {}", attemptId, e);
            // 포기 사유 저장 실패해도 이벤트 처리는 계속 진행
        }
    }
}
