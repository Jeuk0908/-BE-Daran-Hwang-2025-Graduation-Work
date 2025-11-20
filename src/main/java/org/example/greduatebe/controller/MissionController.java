package org.example.greduatebe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.request.MissionStartRequest;
import org.example.greduatebe.dto.response.ApiResponse;
import org.example.greduatebe.dto.response.MissionDetailResponse;
import org.example.greduatebe.dto.response.MissionStartResponse;
import org.example.greduatebe.entity.MissionAttempt;
import org.example.greduatebe.entity.MissionEvent;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;
import org.example.greduatebe.entity.Review;
import org.example.greduatebe.service.EventService;
import org.example.greduatebe.service.MissionService;
import org.example.greduatebe.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 미션 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Slf4j
public class MissionController {

    private final MissionService missionService;
    private final EventService eventService;
    private final ReviewService reviewService;

    /**
     * 미션 시작
     * POST /api/missions/start
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<MissionStartResponse>> startMission(
            @Valid @RequestBody MissionStartRequest request) {

        log.info("POST /api/missions/start - sessionId: {}, missionType: {}",
                request.getSessionId(), request.getMissionType());

        MissionStartResponse response = missionService.startMission(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 미션 시도 상세 조회
     * GET /api/missions/{attemptId}
     */
    @GetMapping("/{attemptId}")
    public ResponseEntity<ApiResponse<MissionDetailResponse>> getMissionDetail(
            @PathVariable String attemptId) {

        log.info("GET /api/missions/{} - Retrieving mission detail", attemptId);

        MissionAttempt missionAttempt = missionService.getMissionAttempt(attemptId);
        List<MissionEvent> events = eventService.getEventsByAttemptId(attemptId);
        Long eventCount = eventService.getEventCountByAttemptId(attemptId);
        Review review = reviewService.getReview(attemptId).orElse(null);

        // MissionDetailResponse 생성
        MissionDetailResponse response = MissionDetailResponse.builder()
                .attemptId(missionAttempt.getAttemptId())
                .sessionId(missionAttempt.getSessionId())
                .missionType(missionAttempt.getMissionType())
                .missionName(missionAttempt.getMissionName())
                .startTime(missionAttempt.getStartTime())
                .endTime(missionAttempt.getEndTime())
                .totalDuration(missionAttempt.getTotalDuration())
                .status(missionAttempt.getStatus())
                .eventCount(eventCount)
                .events(events.stream().map(event ->
                        MissionDetailResponse.EventInfo.builder()
                                .eventId(event.getEventId())
                                .eventType(event.getEventType())
                                .timestamp(event.getTimestamp())
                                .data(event.getData())
                                .build()
                ).collect(Collectors.toList()))
                .review(review != null ? MissionDetailResponse.ReviewInfo.builder()
                        .reviewId(review.getReviewId())
                        .rating(review.getRating())
                        .ratingText(review.getRatingText())
                        .feedback(review.getFeedback())
                        .hasFeedback(review.getHasFeedback())
                        .submittedAt(review.getSubmittedAt())
                        .build() : null)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 미션 시도 목록 조회 (페이징, 필터링)
     * GET /api/missions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MissionDetailResponse>>> listMissions(
            @RequestParam(required = false) MissionType missionType,
            @RequestParam(required = false) MissionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/missions - page: {}, size: {}, missionType: {}, status: {}",
                page, size, missionType, status);

        Pageable pageable = PageRequest.of(page, size);
        Page<MissionAttempt> missionPage = missionService.listMissionAttempts(
                missionType, status, startDate, endDate, pageable);

        // Page<MissionAttempt>를 Page<MissionDetailResponse>로 변환
        Page<MissionDetailResponse> responsePage = missionPage.map(attempt -> {
            Long eventCount = eventService.getEventCountByAttemptId(attempt.getAttemptId());
            Review review = reviewService.getReview(attempt.getAttemptId()).orElse(null);

            return MissionDetailResponse.builder()
                    .attemptId(attempt.getAttemptId())
                    .sessionId(attempt.getSessionId())
                    .missionType(attempt.getMissionType())
                    .missionName(attempt.getMissionName())
                    .startTime(attempt.getStartTime())
                    .endTime(attempt.getEndTime())
                    .totalDuration(attempt.getTotalDuration())
                    .status(attempt.getStatus())
                    .eventCount(eventCount)
                    .review(review != null ? MissionDetailResponse.ReviewInfo.builder()
                            .reviewId(review.getReviewId())
                            .rating(review.getRating())
                            .ratingText(review.getRatingText())
                            .feedback(review.getFeedback())
                            .hasFeedback(review.getHasFeedback())
                            .submittedAt(review.getSubmittedAt())
                            .build() : null)
                    .build();
        });

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }
}
