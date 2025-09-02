package com.handederelii.bom_project.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthException ex, ServletWebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "AUTH_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex, ServletWebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", ex.getMessage(), request);
    }

    @ExceptionHandler(RedisWriteException.class)
    public ResponseEntity<ErrorResponse> handleRedisWrite(RedisWriteException ex, ServletWebRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "REDIS_WRITE_FAILED", ex.getMessage(), request);
    }

    // Ortak builder
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String errorCode, String message, ServletWebRequest request) {
        ErrorResponse error = new ErrorResponse(
                message,
                errorCode,
                request.getRequest().getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
