package com.project.streamingservice.exception;

import com.project.common.exception.RestApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StreamingExceptionHandler {

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<RestApiException> handleVideoNotFoundException(VideoNotFoundException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(restApiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserHistoryNotFoundException.class)
    public ResponseEntity<RestApiException> handleUserHistoryNotFoundException(UserHistoryNotFoundException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(restApiException, HttpStatus.NOT_FOUND);
    }
}
