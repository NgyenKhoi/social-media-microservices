package com.nguyenkhoi.auth_service.controller;

import com.nguyenkhoi.auth_service.dto.ApiResponse;
import com.nguyenkhoi.auth_service.dto.request.OAuth2CallbackRequest;
import com.nguyenkhoi.auth_service.dto.request.OAuth2LinkRequest;
import com.nguyenkhoi.auth_service.dto.response.AuthResponse;
import com.nguyenkhoi.auth_service.dto.response.OAuth2AuthUrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class OAuth2Controller {

    // TODO: Inject services when implemented
    // private final OAuth2Service oauth2Service;

    @GetMapping("/google/login")
    public ResponseEntity<ApiResponse<OAuth2AuthUrlResponse>> initiateGoogleLogin(
            @RequestParam(required = false) String redirectUri) {
        log.info("Initiating Google OAuth login");
        
        // TODO: Implement actual Google OAuth initiation
        // OAuth2AuthUrlResponse response = oauth2Service.getGoogleAuthorizationUrl(redirectUri);
        
        // Temporary mock response for compilation
        OAuth2AuthUrlResponse mockResponse = OAuth2AuthUrlResponse.builder()
                .authUrl("https://accounts.google.com/o/oauth2/v2/auth?client_id=mock&redirect_uri=mock&scope=openid%20profile%20email&response_type=code&state=mock")
                .state("mock-state")
                .provider("google")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Google OAuth URL generated", mockResponse));
    }

    @PostMapping("/google/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> handleGoogleCallback(
            @Valid @RequestBody OAuth2CallbackRequest request,
            HttpServletRequest httpRequest) {
        log.info("Handling Google OAuth callback with code: {}", request.getCode());
        
        // TODO: Implement actual Google OAuth callback handling
        // AuthResponse response = oauth2Service.handleGoogleCallback(request);
        
        // Temporary mock response for compilation
        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("google-mock-access-token")
                .refreshToken("google-mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(AuthResponse.UserInfo.builder()
                        .id("google-user-id")
                        .username("googleuser")
                        .email("google@example.com")
                        .enabled(true)
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Google OAuth login successful", mockResponse));
    }

    @PostMapping("/google/link")
    public ResponseEntity<ApiResponse<Void>> linkGoogleAccount(
            @Valid @RequestBody OAuth2LinkRequest request,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Linking Google account");
        
        // TODO: Implement actual Google account linking
        // oauth2Service.linkGoogleAccount(request, authHeader);
        
        return ResponseEntity.ok(ApiResponse.success("Google account linked successfully", null));
    }

    @DeleteMapping("/google/unlink")
    public ResponseEntity<ApiResponse<Void>> unlinkGoogleAccount(@RequestHeader("Authorization") String authHeader) {
        log.info("Unlinking Google account");
        
        // TODO: Implement actual Google account unlinking
        // oauth2Service.unlinkGoogleAccount(authHeader);
        
        return ResponseEntity.ok(ApiResponse.success("Google account unlinked successfully", null));
    }
}