package org.project.paysystem.exception;

public class CCommunicationException extends RuntimeException {

    // 기본 생성자
    public CCommunicationException() {
        super("Communication error occurred");
    }

    // 메시지를 받는 생성자
    public CCommunicationException(String message) {
        super(message);
    }

    // 메시지와 원인(cause)을 받는 생성자
    public CCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

