package com.nguyenkhoi.auth_service.config;

import com.nguyenkhoi.auth_service.service.OAuth2ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OAuth2ClientConfig {

    @Value("${oauth2.google.client-id}")
    private String googleClientId;
    
    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;
    
    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;
    
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CommandLineRunner initializeOAuth2Clients(OAuth2ClientService clientService) {
        return args -> {
            // Initialize Google OAuth2 client if not exists
            if (googleClientId != null && !googleClientId.isEmpty()) {
                RegisteredClient existingClient = clientService.findByClientId("google-oauth2");
                if (existingClient == null) {
                    RegisteredClient googleClient = RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId("google-oauth2")
                            .clientSecret(passwordEncoder().encode("google-oauth2-secret"))
                            .clientName("Google OAuth2 Client")
                            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                            .redirectUri(googleRedirectUri)
                            .redirectUri("http://localhost:3000/auth/callback/google") // Frontend callback
                            .scope(OidcScopes.OPENID)
                            .scope(OidcScopes.PROFILE)
                            .scope(OidcScopes.EMAIL)
                            .scope("read")
                            .scope("write")
                            .tokenSettings(TokenSettings.builder()
                                    .accessTokenTimeToLive(Duration.ofSeconds(accessTokenExpiration))
                                    .refreshTokenTimeToLive(Duration.ofSeconds(refreshTokenExpiration))
                                    .reuseRefreshTokens(false) // Enable refresh token rotation
                                    .build())
                            .clientSettings(ClientSettings.builder()
                                    .requireAuthorizationConsent(false)
                                    .requireProofKey(true) // Enable PKCE for security
                                    .build())
                            .build();
                    
                    clientService.save(googleClient);
                    log.info("Initialized Google OAuth2 client");
                } else {
                    log.info("Google OAuth2 client already exists");
                }
            } else {
                log.warn("Google OAuth2 client ID not configured, skipping client initialization");
            }
            
            // Initialize a default web client for testing
            RegisteredClient existingWebClient = clientService.findByClientId("web-client");
            if (existingWebClient == null) {
                RegisteredClient webClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("web-client")
                        .clientSecret(passwordEncoder().encode("web-client-secret"))
                        .clientName("Web Application Client")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .redirectUri("http://localhost:3000/auth/callback")
                        .redirectUri("http://localhost:8080/login/oauth2/code/web-client")
                        .scope(OidcScopes.OPENID)
                        .scope(OidcScopes.PROFILE)
                        .scope(OidcScopes.EMAIL)
                        .scope("read")
                        .scope("write")
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofSeconds(accessTokenExpiration))
                                .refreshTokenTimeToLive(Duration.ofSeconds(refreshTokenExpiration))
                                .reuseRefreshTokens(false)
                                .build())
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .requireProofKey(true)
                                .build())
                        .build();
                
                clientService.save(webClient);
                log.info("Initialized Web Application client");
            } else {
                log.info("Web Application client already exists");
            }
        };
    }
}