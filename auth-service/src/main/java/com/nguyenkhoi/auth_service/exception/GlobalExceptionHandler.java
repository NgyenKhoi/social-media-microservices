package com.nguyenkhoi.auth_service.exception;

import com.nguyenkhoi.auth_service.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(400)
                .message("Validation failed")
                .data(errors)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex, WebRequest request) {
        log.error("Application error: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        HttpStatus status = switch (ex.getErrorCode()) {
            case USER_NOTEXISTED, AUTHENTICATION_INVALID -> HttpStatus.UNAUTHORIZED;
            case USER_EXISTED, EMAIL_EXISTED -> HttpStatus.CONFLICT;
            case TOKEN_INVALID, TOKEN_EXPIRED, JWT_EXCEPTION -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.unauthorized("Invalid email or password"));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("Username not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.unauthorized("Invalid email or password"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("An unexpected error occurred"));
    }
}