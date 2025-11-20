package org.example.greduatebe.exception;

/**
 * 미션을 찾을 수 없을 때 발생하는 예외
 */
public class MissionNotFoundException extends RuntimeException {

    public MissionNotFoundException(String message) {
        super(message);
    }

    public MissionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
