package com.nguyenkhoi.api_gateway.filter;

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import com.nguyenkhoi.api_gateway.config.GatewayProperties;
import org.springframework.core.io.buffer.*;
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtTokenValidator jwtTokenValidator;

    public AuthenticationFilter(JwtTokenValidator jwtTokenValidator, GatewayProperties gatewayProperties) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
        this.gatewayProperties = gatewayProperties;
    }

    private final GatewayProperties gatewayProperties;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if (isSecuredEndpoint(request)) {   
                return validateTokenAndProceed(exchange, chain);
            }
            
            return chain.filter(exchange);
        };
    }

    private Mono<Void> validateTokenAndProceed(ServerWebExchange exchange, 
                                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return handleError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
        }

        String jwt = authHeader.substring(7);
        
        try {
            Claims claims = jwtTokenValidator.validateToken(jwt);
            ServerHttpRequest modifiedRequest = addUserContextHeaders(request, claims);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (JwtException e) {
            return handleError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return handleError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private ServerHttpRequest addUserContextHeaders(ServerHttpRequest request, Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        
        return request.mutate()
            .header("X-User-Id", claims.getSubject())
            .header("X-User-Email", claims.get("email", String.class))
            .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
            .header("X-Session-Id", claims.get("session_id", String.class))
            .build();
    }

    private Mono<Void> handleError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorBody = String.format(
            "{\"error\": \"%s\", \"status\": %d, \"timestamp\": \"%s\"}", 
            message, 
            status.value(),
            java.time.Instant.now().toString()
        );
        
        DataBuffer buffer = 
            response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    private boolean isSecuredEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return gatewayProperties.getSecurity().getOpenEndpoints().stream()
            .noneMatch(openEndpoint -> path.startsWith(openEndpoint));
    }

    public static class Config {
    }
}
