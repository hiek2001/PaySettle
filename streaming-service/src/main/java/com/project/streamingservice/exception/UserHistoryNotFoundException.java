package com.project.streamingservice.exception;

public class UserHistoryNotFoundException extends RuntimeException {
    public UserHistoryNotFoundException(String message) {
        super(message);
    }
}
