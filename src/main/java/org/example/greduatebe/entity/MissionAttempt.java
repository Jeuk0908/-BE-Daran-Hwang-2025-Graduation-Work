package org.example.greduatebe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 미션 시도 정보를 저장하는 Entity
 */
@Entity
@Table(name = "mission_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", unique = true, nullable = false, length = 50)
    private String attemptId;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false, length = 20)
    private MissionType missionType;

    @Column(name = "mission_name", nullable = false, length = 100)
    private String missionName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_duration", precision = 10, scale = 3)
    private BigDecimal totalDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MissionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToMany(mappedBy = "missionAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MissionEvent> events = new ArrayList<>();

    @OneToOne(mappedBy = "missionAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;
}
