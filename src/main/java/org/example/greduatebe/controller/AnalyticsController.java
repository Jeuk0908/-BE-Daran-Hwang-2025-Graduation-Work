package org.example.greduatebe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.response.ApiResponse;
import org.example.greduatebe.entity.MissionType;
import org.example.greduatebe.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 분석 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * 미션 분석 데이터 조회
     * GET /api/analytics/missions/{missionType}
     */
    @GetMapping("/missions/{missionType}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMissionAnalytics(
            @PathVariable MissionType missionType) {

        log.info("GET /api/analytics/missions/{}", missionType);

        Map<String, Object> analytics = analyticsService.getMissionAnalytics(missionType);

        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
