package org.example.greduatebe.repository;

import org.example.greduatebe.entity.MissionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MissionEvent Repository
 */
@Repository
public interface MissionEventRepository extends JpaRepository<MissionEvent, Long> {

    /**
     * attemptId로 이벤트 목록 조회 (시간순 정렬)
     * @param attemptId 미션 시도 ID
     * @return MissionEvent 목록
     */
    List<MissionEvent> findByAttemptIdOrderByTimestampAsc(String attemptId);

    /**
     * attemptId로 이벤트 개수 조회
     * @param attemptId 미션 시도 ID
     * @return 이벤트 개수
     */
    Long countByAttemptId(String attemptId);

    /**
     * attemptId와 eventType으로 이벤트 목록 조회
     * @param attemptId 미션 시도 ID
     * @param eventType 이벤트 타입
     * @return MissionEvent 목록
     */
    List<MissionEvent> findByAttemptIdAndEventTypeOrderByTimestampAsc(String attemptId, String eventType);
}
