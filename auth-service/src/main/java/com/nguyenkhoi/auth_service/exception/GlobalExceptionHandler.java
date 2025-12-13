package com.nguyenkhoi.auth_service.exception;

import org.hibernate.LazyInitializationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nguyenkhoi.auth_service.dto.ApiResponse;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(e.getErrorCode().getCode());
        apiResponse.setMessage(e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getStatusCode()).body(apiResponse);
    }

    // Handle spring validation exception
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.VALIDATION_VIOLATED.getCode());
        String fieldError = e.getFieldError() != null ? e.getFieldError().getDefaultMessage() : "Unknown field";
        apiResponse.setMessage(ErrorCode.VALIDATION_VIOLATED.getMessage() + " " + fieldError);
        return ResponseEntity.status(ErrorCode.VALIDATION_VIOLATED.getStatusCode()).body(apiResponse);
    }

    // Handle LazyInitializationException (Hibernate lazy loading issue)
    @ExceptionHandler(value = LazyInitializationException.class)
    ResponseEntity<ApiResponse<Void>> handleLazyInitializationException(LazyInitializationException e) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNEXPECTED_EXCEPTION.getCode());
        apiResponse.setMessage("Data loading error: " + e.getMessage());
        return ResponseEntity.status(ErrorCode.UNEXPECTED_EXCEPTION.getStatusCode()).body(apiResponse);
    }

    // Handle NoSuchElementException (e.g., resource not found)
    @ExceptionHandler(value = NoSuchElementException.class)
    ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(NoSuchElementException e) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        apiResponse.setMessage(e.getMessage() != null ? e.getMessage() : "Resource not found");
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode()).body(apiResponse);
    }

    // Handle IllegalArgumentException (e.g., invalid UUID, invalid request)
    @ExceptionHandler(value = IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.INVALID_REQUEST.getCode());
        apiResponse.setMessage(e.getMessage() != null ? e.getMessage() : "Invalid request");
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatusCode()).body(apiResponse);
    }

    // Handle NullPointerException
    @ExceptionHandler(value = NullPointerException.class)
    ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException e) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNEXPECTED_EXCEPTION.getCode());
        apiResponse.setMessage("Data loading error: Null pointer exception. " + (e.getMessage() != null ? e.getMessage() : ""));
        e.printStackTrace(); // Log stack trace for debugging
        return ResponseEntity.status(ErrorCode.UNEXPECTED_EXCEPTION.getStatusCode()).body(apiResponse);
    }

    // Handle unexpected exception (enable for debugging)
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUncategorizedException(Exception e) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNEXPECTED_EXCEPTION.getCode());
        apiResponse.setMessage("Unexpected error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        e.printStackTrace(); // Log full stack trace for debugging
        return ResponseEntity.status(ErrorCode.UNEXPECTED_EXCEPTION.getStatusCode()).body(apiResponse);
    }

}
