package com.nguyenkhoi.auth_service.controller;

import com.nguyenkhoi.auth_service.dto.*;
import com.nguyenkhoi.auth_service.dto.request.LoginRequest;
import com.nguyenkhoi.auth_service.dto.request.RefreshTokenRequest;
import com.nguyenkhoi.auth_service.dto.request.RegisterRequest;
import com.nguyenkhoi.auth_service.dto.response.AuthResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        // TODO: Implement actual login logic
        // AuthResponse response = authService.login(request);
        
        // Temporary mock response for compilation
        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes
                .user(AuthResponse.UserInfo.builder()
                        .id("mock-user-id")
                        .username("mockuser")
                        .email(request.getEmail())
                        .enabled(true)
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", mockResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        // TODO: Implement actual registration logic
        // AuthResponse response = authService.register(request);
        
        // Temporary mock response for compilation
        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(AuthResponse.UserInfo.builder()
                        .id("mock-user-id")
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .enabled(true)
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Registration successful", mockResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        
        // TODO: Implement actual token refresh logic
        // AuthResponse response = authService.refreshToken(request.getRefreshToken());
        
        // Temporary mock response for compilation
        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("new-mock-access-token")
                .refreshToken("new-mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", mockResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout attempt");
        
        // TODO: Implement actual logout logic
        // authService.logout(authHeader);
        
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.info("Get current user info");
        
        // TODO: Implement actual user info retrieval
        // UserInfo userInfo = userService.getCurrentUser(authHeader);
        
        // Temporary mock response for compilation
        AuthResponse.UserInfo mockUser = AuthResponse.UserInfo.builder()
                .id("mock-user-id")
                .username("mockuser")
                .email("mock@example.com")
                .enabled(true)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("User info retrieved", mockUser));
    }
}