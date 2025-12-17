package com.nguyenkhoi.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    //just need only 1 success static method
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(500, message, null, LocalDateTime.now());
    }
}
