package org.example.greduatebe.exception;

/**
 * 유효하지 않은 이벤트일 때 발생하는 예외
 */
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }

    public InvalidEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
