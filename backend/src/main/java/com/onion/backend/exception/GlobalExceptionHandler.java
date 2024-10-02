package com.onion.backend.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice  // 모든 컨트롤러에서 발생하는 예외를 처리하는 어드바이스
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 1. IllegalArgumentException 처리
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
//        Map<String, Object> responseBody = new HashMap<>();
//        responseBody.put("timestamp", LocalDateTime.now());
//        responseBody.put("message", ex.getMessage());
//        responseBody.put("details", request.getDescription(false));
//
//        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
//    }

    // 2. NullPointerException 처리
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointerException(NullPointerException ex, WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Null value encountered");
        responseBody.put("details", request.getDescription(false));

        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 3. Custom Exception 처리
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleCustomException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", ex.getMessage());
        responseBody.put("details", request.getDescription(false));

        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    // 4. Default 예외 처리 (모든 예외의 기본 처리)
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
//        Map<String, Object> responseBody = new HashMap<>();
//        responseBody.put("timestamp", LocalDateTime.now());
//        responseBody.put("message", "An unexpected error occurred");
//        responseBody.put("details", request.getDescription(false));
//
//        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

}