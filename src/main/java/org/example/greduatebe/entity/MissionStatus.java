package org.example.greduatebe.entity;

/**
 * 미션 상태를 정의하는 Enum
 */
public enum MissionStatus {
    /**
     * 대기 중
     */
    PENDING,

    /**
     * 진행 중
     */
    IN_PROGRESS,

    /**
     * 완료됨
     */
    COMPLETED,

    /**
     * 포기함
     */
    QUITTED,

    /**
     * 만료됨
     */
    EXPIRED
}
