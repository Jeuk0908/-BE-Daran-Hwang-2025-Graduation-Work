package org.example.greduatebe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.request.ReviewSubmitRequest;
import org.example.greduatebe.dto.response.ApiResponse;
import org.example.greduatebe.dto.response.MissionDetailResponse;
import org.example.greduatebe.entity.Review;
import org.example.greduatebe.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 리뷰 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/missions/{attemptId}/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 제출
     * POST /api/missions/{attemptId}/review
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MissionDetailResponse.ReviewInfo>> submitReview(
            @PathVariable String attemptId,
            @Valid @RequestBody ReviewSubmitRequest request) {

        log.info("POST /api/missions/{}/review - rating: {}", attemptId, request.getRating());

        Review review = reviewService.submitReview(attemptId, request);

        MissionDetailResponse.ReviewInfo response = MissionDetailResponse.ReviewInfo.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .ratingText(review.getRatingText())
                .feedback(review.getFeedback())
                .hasFeedback(review.getHasFeedback())
                .submittedAt(review.getSubmittedAt())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 리뷰 조회
     * GET /api/missions/{attemptId}/review
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MissionDetailResponse.ReviewInfo>> getReview(
            @PathVariable String attemptId) {

        log.info("GET /api/missions/{}/review", attemptId);

        Review review = reviewService.getReview(attemptId)
                .orElseThrow(() -> new RuntimeException("Review not found for attemptId: " + attemptId));

        MissionDetailResponse.ReviewInfo response = MissionDetailResponse.ReviewInfo.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .ratingText(review.getRatingText())
                .feedback(review.getFeedback())
                .hasFeedback(review.getHasFeedback())
                .submittedAt(review.getSubmittedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
