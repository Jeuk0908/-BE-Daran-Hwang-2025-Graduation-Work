package org.example.greduatebe.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 미션 타입을 정의하는 Enum
 */
public enum MissionType {
    /**
     * 포트폴리오 미션
     */
    PORTFOLIO,

    /**
     * 어휘 미션
     */
    VOCABULARY;

    /**
     * JSON 역직렬화 시 대소문자 구분 없이 처리
     */
    @JsonCreator
    public static MissionType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (MissionType type : MissionType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid MissionType: " + value +
                ". Allowed values: PORTFOLIO, VOCABULARY");
    }

    /**
     * JSON 직렬화 시 대문자로 출력
     */
    @JsonValue
    public String toValue() {
        return this.name();
    }
}
