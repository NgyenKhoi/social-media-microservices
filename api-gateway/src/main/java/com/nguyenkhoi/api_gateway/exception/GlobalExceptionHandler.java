package com.nguyenkhoi.api_gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        HttpStatus status = determineHttpStatus(ex);
        String message = determineErrorMessage(ex, status);
        String requestPath = exchange.getRequest().getPath().value();
        
        response.setStatusCode(status);
        
        String errorBody = createErrorResponse(message, status, requestPath);
        var buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            var statusCode = ((org.springframework.web.server.ResponseStatusException) ex).getStatusCode();
            return HttpStatus.valueOf(statusCode.value());
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineErrorMessage(Throwable ex, HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> "Authentication required";
            case FORBIDDEN -> "Access denied";
            case NOT_FOUND -> "Resource not found";
            case TOO_MANY_REQUESTS -> "Rate limit exceeded";
            case SERVICE_UNAVAILABLE -> "Service temporarily unavailable";
            default -> "An error occurred while processing your request";
        };
    }

    private String createErrorResponse(String message, HttpStatus status, String path) {
        return String.format(
            "{\"error\":{\"message\":\"%s\",\"status\":%d,\"timestamp\":\"%s\",\"path\":\"%s\"}}",
            message,
            status.value(),
            Instant.now().toString(),
            path
        );
    }
}