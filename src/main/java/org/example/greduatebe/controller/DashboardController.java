package org.example.greduatebe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.response.dashboard.*;
import org.example.greduatebe.service.MissionAnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 대시보드 Controller
 * Thymeleaf 템플릿으로 SSR 방식으로 렌더링
 * DASHBOARD_GUIDE.md Section 4 참고
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final MissionAnalysisService analysisService;

    /**
     * 메인 대시보드 페이지
     * DASHBOARD_GUIDE.md Section 4.1 참고
     *
     * @param model Thymeleaf Model
     * @return templates/dashboard/main.html
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("📊 [Dashboard] Main dashboard page requested");

        try {
            // 전체 통계
            DashboardOverviewStatsDto overviewStats = analysisService.getOverviewStats();
            model.addAttribute("overviewStats", overviewStats);
            log.debug("Overview stats loaded: totalAttempts={}, completionRate={}%",
                    overviewStats.getTotalAttempts(), overviewStats.getOverallCompletionRate());

            // 미션별 완료율
            List<MissionCompletionRateDto> completionRates = analysisService.getCompletionRates();
            model.addAttribute("completionRates", completionRates);
            log.debug("Completion rates loaded: {} mission types", completionRates.size());

            // 최근 미션 기록 (최근 20개)
            List<RecentAttemptDto> recentAttempts = analysisService.getRecentAttempts(20);
            model.addAttribute("recentAttempts", recentAttempts);
            log.debug("Recent attempts loaded: {} attempts", recentAttempts.size());

            // 시간대별 분포
            List<HourlyDistributionDto> hourlyDistribution = analysisService.getHourlyDistribution();
            model.addAttribute("hourlyDistribution", hourlyDistribution);
            log.debug("Hourly distribution loaded: {} time ranges", hourlyDistribution.size());

            // 최근 후기 (최근 10개)
            List<ReviewSummaryDto> recentReviews = analysisService.getRecentReviews(10);
            model.addAttribute("recentReviews", recentReviews);
            log.debug("Recent reviews loaded: {} reviews", recentReviews.size());

            log.info("✅ [Dashboard] Main dashboard loaded successfully");
            return "dashboard/main";

        } catch (Exception e) {
            log.error("❌ [Dashboard] Error loading main dashboard", e);
            model.addAttribute("errorMessage", "대시보드 로딩 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 미션 시도 상세 페이지
     * DASHBOARD_GUIDE.md Section 4.2 참고
     *
     * @param attemptId Attempt ID
     * @param model Thymeleaf Model
     * @return templates/dashboard/attempt-detail.html
     */
    @GetMapping("/dashboard/attempt/{attemptId}")
    public String attemptDetail(@PathVariable String attemptId, Model model) {
        log.info("📋 [Dashboard] Attempt detail page requested - attemptId: {}", attemptId);

        try {
            // 미션 시도 상세 조회
            AttemptDetailDto attemptDetail = analysisService.getAttemptDetail(attemptId);

            if (attemptDetail == null) {
                log.warn("⚠️ [Dashboard] Attempt not found - attemptId: {}", attemptId);
                model.addAttribute("errorMessage", "해당 미션 기록을 찾을 수 없습니다: " + attemptId);
                return "error";
            }

            model.addAttribute("attempt", attemptDetail);
            model.addAttribute("timeline", attemptDetail.getTimelineEvents());
            model.addAttribute("stepDetails", attemptDetail.getStepDetails());

            log.info("✅ [Dashboard] Attempt detail loaded - attemptId: {}, missionType: {}, status: {}",
                    attemptId, attemptDetail.getMissionType(), attemptDetail.getStatus());

            return "dashboard/attempt-detail";

        } catch (Exception e) {
            log.error("❌ [Dashboard] Error loading attempt detail - attemptId: {}", attemptId, e);
            model.addAttribute("errorMessage", "미션 상세 정보 로딩 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 전체 후기 페이지
     *
     * @param rating 평점 필터 (optional)
     * @param missionType 미션 타입 필터 (optional)
     * @param hasFeedback 후기 유무 필터 (optional)
     * @param model Thymeleaf Model
     * @return templates/dashboard/reviews.html
     */
    @GetMapping("/dashboard/reviews")
    public String reviews(
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String missionType,
            @RequestParam(required = false) Boolean hasFeedback,
            Model model) {
        log.info("💬 [Dashboard] Reviews page requested - rating: {}, missionType: {}, hasFeedback: {}",
                rating, missionType, hasFeedback);

        try {
            // 전체 후기 조회 (필터링 적용)
            List<ReviewSummaryDto> reviews = analysisService.getAllReviews(rating, missionType, hasFeedback);
            model.addAttribute("reviews", reviews);
            log.debug("Reviews loaded: {} reviews", reviews.size());

            // 후기 통계
            ReviewStatisticsDto statistics = analysisService.getReviewStatistics();
            model.addAttribute("statistics", statistics);
            log.debug("Review statistics loaded: total={}, avgRating={}",
                    statistics.getTotalReviews(), statistics.getAvgRating());

            // 필터 값 유지를 위해 다시 전달
            model.addAttribute("selectedRating", rating);
            model.addAttribute("selectedMissionType", missionType);
            model.addAttribute("selectedHasFeedback", hasFeedback);

            log.info("✅ [Dashboard] Reviews page loaded successfully");
            return "dashboard/reviews";

        } catch (Exception e) {
            log.error("❌ [Dashboard] Error loading reviews page", e);
            model.addAttribute("errorMessage", "후기 로딩 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }
}
