package com.nguyenkhoi.auth_service.controller;

import com.nguyenkhoi.auth_service.dto.ApiResponse;
import com.nguyenkhoi.auth_service.dto.request.OAuth2CallbackRequest;
import com.nguyenkhoi.auth_service.dto.request.OAuth2LinkRequest;
import com.nguyenkhoi.auth_service.dto.response.AuthResponse;
import com.nguyenkhoi.auth_service.dto.response.OAuth2AuthUrlResponse;
import com.nguyenkhoi.auth_service.service.OAuth2Service;
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

    private final OAuth2Service oauth2Service;

    @GetMapping("/google/login")
    public ResponseEntity<ApiResponse<OAuth2AuthUrlResponse>> initiateGoogleLogin(
            @RequestParam(required = false) String redirectUri) {
        log.info("Initiating Google OAuth login");
        
        OAuth2AuthUrlResponse response = oauth2Service.getGoogleAuthorizationUrl(redirectUri);
        
        return ResponseEntity.ok(ApiResponse.success("Google OAuth URL generated", response));
    }

    @PostMapping("/google/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> handleGoogleCallback(
            @Valid @RequestBody OAuth2CallbackRequest request,
            HttpServletRequest httpRequest) {
        log.info("Handling Google OAuth callback with code: {}", request.getCode());
        
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        
        AuthResponse response = oauth2Service.handleGoogleCallback(request, userAgent, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success("Google OAuth login successful", response));
    }

    @PostMapping("/google/link")
    public ResponseEntity<ApiResponse<Void>> linkGoogleAccount(
            @Valid @RequestBody OAuth2LinkRequest request,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Linking Google account");
        
        oauth2Service.linkGoogleAccount(request, authHeader);
        
        return ResponseEntity.ok(ApiResponse.success("Google account linked successfully", null));
    }

    @DeleteMapping("/google/unlink")
    public ResponseEntity<ApiResponse<Void>> unlinkGoogleAccount(@RequestHeader("Authorization") String authHeader) {
        log.info("Unlinking Google account");
        
        oauth2Service.unlinkGoogleAccount(authHeader);
        
        return ResponseEntity.ok(ApiResponse.success("Google account unlinked successfully", null));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}