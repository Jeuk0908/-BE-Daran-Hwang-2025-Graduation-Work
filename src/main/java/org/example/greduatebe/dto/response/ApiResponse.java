package org.example.greduatebe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 응답 래퍼 클래스
 * @param <T> 응답 데이터 타입
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorInfo error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorInfo {
        private String code;
        private String message;
        private String details;
    }

    /**
     * 성공 응답 생성
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 에러 응답 생성
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }

    /**
     * 에러 응답 생성 (상세 정보 포함)
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param details 상세 정보
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message, String details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .build();
    }
}
