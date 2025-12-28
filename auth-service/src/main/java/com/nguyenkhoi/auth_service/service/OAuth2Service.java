package com.nguyenkhoi.auth_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenkhoi.auth_service.dto.request.OAuth2CallbackRequest;
import com.nguyenkhoi.auth_service.dto.request.OAuth2LinkRequest;
import com.nguyenkhoi.auth_service.dto.response.AuthResponse;
import com.nguyenkhoi.auth_service.dto.response.OAuth2AuthUrlResponse;
import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.entities.UserExternalAccount.OAuthProvider;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {

    private final OAuth2UserService oauth2UserService;
    private final JwtTokenService jwtTokenService;
    private final UserSessionService userSessionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    
    private static final String STATE_PREFIX = "oauth2:state:";
    private static final String PKCE_PREFIX = "oauth2:pkce:";
    private static final Duration STATE_EXPIRATION = Duration.ofMinutes(10);

    public OAuth2AuthUrlResponse getGoogleAuthorizationUrl(String redirectUri) {
        log.info("Generating Google OAuth authorization URL");

        if (googleClientId == null || googleClientId.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Google OAuth not configured");
        }

        // Generate state parameter for CSRF protection
        String state = generateSecureRandomString(32);
        
        // Generate PKCE parameters
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        
        // Store state and PKCE verifier in Redis with expiration
        redisTemplate.opsForValue().set(STATE_PREFIX + state, "valid", STATE_EXPIRATION);
        redisTemplate.opsForValue().set(PKCE_PREFIX + state, codeVerifier, STATE_EXPIRATION);

        // Use provided redirect URI or default
        String finalRedirectUri = redirectUri != null ? redirectUri : googleRedirectUri;

        // Build authorization URL
        String authUrl = UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", finalRedirectUri)
                .queryParam("scope", "openid profile email")
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();

        log.info("Generated Google OAuth URL with state: {}", state);

        return OAuth2AuthUrlResponse.builder()
                .authUrl(authUrl)
                .state(state)
                .provider("google")
                .build();
    }

    public AuthResponse handleGoogleCallback(OAuth2CallbackRequest request, String userAgent, String ipAddress) {
        log.info("Handling Google OAuth callback with code: {}", request.getCode());

        // Validate state parameter
        validateState(request.getState());

        // Get PKCE code verifier
        String codeVerifier = getCodeVerifier(request.getState());

        // Exchange authorization code for tokens
        Map<String, Object> tokenResponse = exchangeCodeForTokens(
                request.getCode(), 
                request.getRedirectUri() != null ? request.getRedirectUri() : googleRedirectUri,
                codeVerifier
        );

        // Get user information from Google
        Map<String, Object> userInfo = getUserInfoFromGoogle((String) tokenResponse.get("access_token"));

        // Process OAuth2 user (create or link account)
        AppUser user = oauth2UserService.processOAuth2User(
                OAuthProvider.GOOGLE,
                (String) userInfo.get("id"),
                (String) userInfo.get("email"),
                (String) tokenResponse.get("access_token"),
                (String) tokenResponse.get("refresh_token"),
                tokenResponse.get("expires_in") != null ? ((Number) tokenResponse.get("expires_in")).longValue() : null,
                "openid profile email"
        );

        // Create user session
        String sessionId = userSessionService.createSession(user, userAgent, ipAddress);

        // Generate JWT tokens
        String accessToken = jwtTokenService.generateAccessToken(user, sessionId);
        String refreshToken = jwtTokenService.generateRefreshToken(user, sessionId, ipAddress, userAgent);

        // Clean up temporary data
        cleanupOAuthState(request.getState());

        log.info("Google OAuth login successful for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .enabled(user.getIsEnabled())
                        .build())
                .build();
    }

    public void linkGoogleAccount(OAuth2LinkRequest request, String authHeader) {
        log.info("Linking Google account");

        // Extract user from JWT token
        String token = authHeader.replace("Bearer ", "");
        AppUser user = jwtTokenService.getUserFromToken(token);

        // Validate state parameter
        validateState(request.getState());

        // Get PKCE code verifier
        String codeVerifier = getCodeVerifier(request.getState());

        // Exchange authorization code for tokens
        Map<String, Object> tokenResponse = exchangeCodeForTokens(
                request.getCode(),
                request.getRedirectUri() != null ? request.getRedirectUri() : googleRedirectUri,
                codeVerifier
        );

        // Get user information from Google
        Map<String, Object> userInfo = getUserInfoFromGoogle((String) tokenResponse.get("access_token"));

        // Check if this Google account is already linked to another user
        String googleUserId = (String) userInfo.get("id");
        oauth2UserService.findUserByOAuth(OAuthProvider.GOOGLE, googleUserId)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(user.getId())) {
                        throw new AppException(ErrorCode.OAUTH_ACCOUNT_ALREADY_LINKED, 
                                "This Google account is already linked to another user");
                    }
                });

        // Link the account
        oauth2UserService.createExternalAccount(
                user,
                OAuthProvider.GOOGLE,
                googleUserId,
                (String) userInfo.get("email"),
                (String) tokenResponse.get("access_token"),
                (String) tokenResponse.get("refresh_token"),
                tokenResponse.get("expires_in") != null ? ((Number) tokenResponse.get("expires_in")).longValue() : null,
                "openid profile email"
        );

        // Clean up temporary data
        cleanupOAuthState(request.getState());

        log.info("Google account linked successfully for user: {}", user.getEmail());
    }

    public void unlinkGoogleAccount(String authHeader) {
        log.info("Unlinking Google account");

        // Extract user from JWT token
        String token = authHeader.replace("Bearer ", "");
        AppUser user = jwtTokenService.getUserFromToken(token);

        // Unlink the account
        oauth2UserService.unlinkOAuthAccount(user, OAuthProvider.GOOGLE);

        log.info("Google account unlinked successfully for user: {}", user.getEmail());
    }

    private void validateState(String state) {
        if (state == null || state.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "State parameter is required");
        }

        String storedState = (String) redisTemplate.opsForValue().get(STATE_PREFIX + state);
        if (storedState == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid or expired state parameter");
        }
    }

    private String getCodeVerifier(String state) {
        String codeVerifier = (String) redisTemplate.opsForValue().get(PKCE_PREFIX + state);
        if (codeVerifier == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "PKCE code verifier not found");
        }
        return codeVerifier;
    }

    private Map<String, Object> exchangeCodeForTokens(String code, String redirectUri, String codeVerifier) {
        log.info("Exchanging authorization code for tokens");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);
        params.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.convertValue(jsonNode, Map.class);
                return result;
            } else {
                log.error("Failed to exchange code for tokens: {}", response.getBody());
                throw new AppException(ErrorCode.INVALID_REQUEST, "Failed to exchange authorization code");
            }
        } catch (Exception e) {
            log.error("Error exchanging code for tokens", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "OAuth token exchange failed");
        }
    }

    private Map<String, Object> getUserInfoFromGoogle(String accessToken) {
        log.info("Fetching user info from Google");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GOOGLE_USER_INFO_URL, 
                    HttpMethod.GET, 
                    request, 
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.convertValue(jsonNode, Map.class);
                return result;
            } else {
                log.error("Failed to fetch user info: {}", response.getBody());
                throw new AppException(ErrorCode.INVALID_REQUEST, "Failed to fetch user information");
            }
        } catch (Exception e) {
            log.error("Error fetching user info from Google", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to fetch user information");
        }
    }

    private void cleanupOAuthState(String state) {
        redisTemplate.delete(STATE_PREFIX + state);
        redisTemplate.delete(PKCE_PREFIX + state);
    }

    private String generateSecureRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeVerifier() {
        return generateSecureRandomString(32);
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to generate code challenge");
        }
    }
}