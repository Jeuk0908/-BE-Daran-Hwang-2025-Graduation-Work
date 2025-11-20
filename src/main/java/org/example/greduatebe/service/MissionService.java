package org.example.greduatebe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greduatebe.dto.request.MissionStartRequest;
import org.example.greduatebe.dto.response.MissionDetailResponse;
import org.example.greduatebe.dto.response.MissionStartResponse;
import org.example.greduatebe.entity.MissionAttempt;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;
import org.example.greduatebe.exception.MissionNotFoundException;
import org.example.greduatebe.repository.MissionAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 미션 관련 비즈니스 로직 처리 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MissionService {

    private final MissionAttemptRepository missionAttemptRepository;

    @Value("${websocket.url:ws://localhost:8080/ws}")
    private String websocketUrl;

    @Value("${mission.expires-in:3600000}")
    private Long missionExpiresIn; // 기본 1시간 (밀리초)

    /**
     * 미션 시작
     * @param request 미션 시작 요청
     * @return MissionStartResponse
     */
    @Transactional
    public MissionStartResponse startMission(MissionStartRequest request) {
        log.info("Starting mission - sessionId: {}, missionType: {}",
                request.getSessionId(), request.getMissionType());

        // attemptId 생성 (UUID 기반)
        String attemptId = generateAttemptId();

        // missionName 설정
        String missionName = getMissionName(request.getMissionType());

        // MissionAttempt 엔티티 생성 및 저장
        MissionAttempt missionAttempt = MissionAttempt.builder()
                .attemptId(attemptId)
                .sessionId(request.getSessionId())
                .missionType(request.getMissionType())
                .missionName(missionName)
                .startTime(request.getTimestamp())
                .status(MissionStatus.IN_PROGRESS)
                .build();

        missionAttemptRepository.save(missionAttempt);

        log.info("Mission started successfully - attemptId: {}", attemptId);

        return MissionStartResponse.builder()
                .attemptId(attemptId)
                .wsUrl(websocketUrl)
                .expiresIn(missionExpiresIn)
                .build();
    }

    /**
     * 미션 시도 조회
     * @param attemptId 미션 시도 ID
     * @return MissionAttempt
     */
    @Transactional(readOnly = true)
    public MissionAttempt getMissionAttempt(String attemptId) {
        log.debug("Getting mission attempt - attemptId: {}", attemptId);
        return missionAttemptRepository.findByAttemptId(attemptId)
                .orElseThrow(() -> new MissionNotFoundException("Mission attempt not found: " + attemptId));
    }

    /**
     * 미션 시도 목록 조회 (페이징, 필터링)
     * @param missionType 미션 타입 (선택)
     * @param status 상태 (선택)
     * @param startDate 시작 날짜 (선택)
     * @param endDate 종료 날짜 (선택)
     * @param pageable 페이징 정보
     * @return Page<MissionAttempt>
     */
    @Transactional(readOnly = true)
    public Page<MissionAttempt> listMissionAttempts(
            MissionType missionType,
            MissionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.debug("Listing mission attempts - missionType: {}, status: {}, page: {}",
                missionType, status, pageable.getPageNumber());

        // 임시: 필터링 없이 모든 미션 조회
        // TODO: Specification API를 사용한 동적 쿼리로 개선 필요
        return missionAttemptRepository.findAll(pageable);
    }

    /**
     * 미션 상태 업데이트
     * @param attemptId 미션 시도 ID
     * @param status 새로운 상태
     */
    @Transactional
    public void updateMissionStatus(String attemptId, MissionStatus status) {
        log.info("Updating mission status - attemptId: {}, status: {}", attemptId, status);

        MissionAttempt missionAttempt = getMissionAttempt(attemptId);
        missionAttempt.setStatus(status);
        missionAttemptRepository.save(missionAttempt);
    }

    /**
     * attemptId 생성
     * @return attemptId
     */
    private String generateAttemptId() {
        return "attempt_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 미션 타입에 따른 미션 이름 반환
     * @param missionType 미션 타입
     * @return 미션 이름
     */
    private String getMissionName(MissionType missionType) {
        return switch (missionType) {
            case PORTFOLIO -> "Portfolio Mission";
            case VOCABULARY -> "Vocabulary Mission";
        };
    }
}
