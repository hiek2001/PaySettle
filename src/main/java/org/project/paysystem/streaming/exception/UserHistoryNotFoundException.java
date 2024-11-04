package org.project.paysystem.streaming.exception;

public class UserHistoryNotFoundException extends RuntimeException {
    public UserHistoryNotFoundException(String message) {
        super(message);
    }
}
