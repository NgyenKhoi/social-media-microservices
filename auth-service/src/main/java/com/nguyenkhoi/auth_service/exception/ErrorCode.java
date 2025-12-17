package com.nguyenkhoi.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {

    // Authentication & Authorization
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),
    USER_NOTEXISTED(1003, "User not existed", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1003, "User not existed", HttpStatus.NOT_FOUND),
    USER_EXISTED(1004, "User already existed", HttpStatus.BAD_REQUEST),
    USER_INACTIVE(1005, "User account is inactive", HttpStatus.BAD_REQUEST),

    // JWT Handling
    JWT_EXCEPTION(1006, "Jwt handling exception", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_INVALID(1007, "The refresh token not exist in db", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1007, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1008, "The refresh token already expired", HttpStatus.UNAUTHORIZED),
    TOKEN_REUSED(1009, "The refresh token already used", HttpStatus.UNAUTHORIZED),

    // Password & Credentials
    PASSWORD_NOTMATCH(1010, "Current password does not match", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1011, "Email already existed", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1012, "Password must be at least 6 characters long", HttpStatus.BAD_REQUEST),

    // Validation
    VALIDATION_VIOLATED(2001, "Validation violated:", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(2002, "Invalid request", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_INVALID(2003, "Authentication request invalid, missing email or username", HttpStatus.BAD_REQUEST),

    // Role & Permission
    ROLE_NOTEXISTED(3001, "The role not exist", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_EXISTED(3001, "The role not exist", HttpStatus.INTERNAL_SERVER_ERROR),

    // OAuth2
    OAUTH_ACCOUNT_ALREADY_LINKED(4001, "OAuth account already linked", HttpStatus.BAD_REQUEST),
    OAUTH_ACCOUNT_NOT_FOUND(4002, "OAuth account not found", HttpStatus.NOT_FOUND),

    // Not Implemented
    NOT_IMPLEMENTED(5001, "Feature not implemented", HttpStatus.NOT_IMPLEMENTED),

    // Generic Errors
    INTERNAL_SERVER_ERROR(9000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNEXPECTED_EXCEPTION(9999, "we get unexpected exception", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

}
