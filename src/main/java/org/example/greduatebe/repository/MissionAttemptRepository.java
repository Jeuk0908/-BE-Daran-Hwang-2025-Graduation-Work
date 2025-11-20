package org.example.greduatebe.repository;

import org.example.greduatebe.entity.MissionAttempt;
import org.example.greduatebe.entity.MissionStatus;
import org.example.greduatebe.entity.MissionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * MissionAttempt Repository
 */
@Repository
public interface MissionAttemptRepository extends JpaRepository<MissionAttempt, Long> {

    /**
     * attemptId로 미션 시도 조회
     * @param attemptId 미션 시도 ID
     * @return MissionAttempt
     */
    Optional<MissionAttempt> findByAttemptId(String attemptId);

    /**
     * 필터 조건으로 미션 시도 목록 조회 (페이징)
     * - null 파라미터 처리를 위해 동적 쿼리 대신 단순화
     */
    Page<MissionAttempt> findAll(Pageable pageable);

    /**
     * sessionId로 미션 시도 목록 조회
     * @param sessionId 세션 ID
     * @return MissionAttempt 목록
     */
    Page<MissionAttempt> findBySessionIdOrderByStartTimeDesc(String sessionId, Pageable pageable);
}
