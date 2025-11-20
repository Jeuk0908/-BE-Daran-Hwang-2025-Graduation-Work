package org.example.greduatebe.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 미션 이벤트 정보를 저장하는 Entity
 */
@Entity
@Table(name = "mission_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false, length = 50)
    private String eventId;

    @Column(name = "attempt_id", nullable = false, length = 50)
    private String attemptId;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Type(JsonType.class)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> data;

    @CreationTimestamp
    @Column(name = "received_at", updatable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processing_time")
    private Long processingTime;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", referencedColumnName = "attempt_id", insertable = false, updatable = false)
    private MissionAttempt missionAttempt;
}
