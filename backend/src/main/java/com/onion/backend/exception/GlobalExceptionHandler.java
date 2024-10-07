package com.onion.backend.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice  // 모든 컨트롤러에서 발생하는 예외를 처리하는 어드바이스
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    //  Custom Exception 처리
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleCustomException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", ex.getMessage());
        responseBody.put("details", request.getDescription(false));

        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }
    // forbidden exception 처리
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException ex, WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", ex.getMessage());
        responseBody.put("details", request.getDescription(false));

        return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
    }



}