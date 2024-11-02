package com.project.streamingservice.exception;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(String message) {
        super(message);
    }
}
