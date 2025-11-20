package org.example.greduatebe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.request.ReviewSubmitRequest;
import org.example.greduatebe.entity.MissionAttempt;
import org.example.greduatebe.entity.Review;
import org.example.greduatebe.exception.InvalidRequestException;
import org.example.greduatebe.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 리뷰 관련 비즈니스 로직 처리 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MissionService missionService;

    /**
     * 리뷰 제출
     * @param attemptId 미션 시도 ID
     * @param request 리뷰 제출 요청
     * @return Review
     */
    @Transactional
    public Review submitReview(String attemptId, ReviewSubmitRequest request) {
        log.info("Submitting review - attemptId: {}, rating: {}", attemptId, request.getRating());

        // MissionAttempt 존재 여부 확인
        MissionAttempt missionAttempt = missionService.getMissionAttempt(attemptId);

        // 이미 리뷰가 존재하는지 확인
        if (reviewRepository.existsByAttemptId(attemptId)) {
            throw new InvalidRequestException("Review already exists for attemptId: " + attemptId);
        }

        // reviewId 생성
        String reviewId = generateReviewId();

        // Review 엔티티 생성 및 저장
        Review review = Review.builder()
                .reviewId(reviewId)
                .attemptId(attemptId)
                .rating(request.getRating())
                .ratingText(request.getRatingText())
                .feedback(request.getFeedback())
                .hasFeedback(request.getHasFeedback())
                .submittedAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        log.info("Review submitted successfully - reviewId: {}, attemptId: {}", reviewId, attemptId);

        return savedReview;
    }

    /**
     * 리뷰 조회 (attemptId)
     * @param attemptId 미션 시도 ID
     * @return Review
     */
    @Transactional(readOnly = true)
    public Optional<Review> getReview(String attemptId) {
        log.debug("Getting review by attemptId: {}", attemptId);
        return reviewRepository.findByAttemptId(attemptId);
    }

    /**
     * 리뷰 조회 (reviewId)
     * @param reviewId 리뷰 ID
     * @return Review
     */
    @Transactional(readOnly = true)
    public Optional<Review> getReviewById(String reviewId) {
        log.debug("Getting review by reviewId: {}", reviewId);
        return reviewRepository.findByReviewId(reviewId);
    }

    /**
     * 리뷰 존재 여부 확인
     * @param attemptId 미션 시도 ID
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean hasReview(String attemptId) {
        return reviewRepository.existsByAttemptId(attemptId);
    }

    /**
     * reviewId 생성
     * @return reviewId
     */
    private String generateReviewId() {
        return "review_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 리뷰 직접 저장 (이벤트 처리용)
     * WebSocket 이벤트로부터 리뷰를 생성할 때 사용
     * @param review Review 엔티티
     * @return 저장된 Review
     */
    @Transactional
    public Review saveReviewDirectly(Review review) {
        log.info("Saving review directly - reviewId: {}, attemptId: {}",
                review.getReviewId(), review.getAttemptId());
        return reviewRepository.save(review);
    }
}
