package org.example.greduatebe.exception;

/**
 * 유효하지 않은 요청일 때 발생하는 예외
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
