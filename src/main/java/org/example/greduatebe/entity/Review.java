package org.example.greduatebe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 미션 리뷰 정보를 저장하는 Entity
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", unique = true, nullable = false, length = 50)
    private String reviewId;

    @Column(name = "attempt_id", nullable = false, length = 50)
    private String attemptId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "rating_text", nullable = false, length = 20)
    private String ratingText;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "has_feedback", nullable = false)
    private Boolean hasFeedback;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 연관관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", referencedColumnName = "attempt_id", insertable = false, updatable = false)
    private MissionAttempt missionAttempt;
}
