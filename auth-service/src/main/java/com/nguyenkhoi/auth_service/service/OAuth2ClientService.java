package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.entities.Oauth2Client;
import com.nguyenkhoi.auth_service.repository.Oauth2ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ClientService implements RegisteredClientRepository {

    private final Oauth2ClientRepository oauth2ClientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        Oauth2Client oauth2Client = new Oauth2Client();
        oauth2Client.setClientId(registeredClient.getClientId());
        oauth2Client.setClientSecret(registeredClient.getClientSecret());
        oauth2Client.setClientName(registeredClient.getClientName());
        
        // Convert authentication methods to string
        String authMethods = registeredClient.getClientAuthenticationMethods()
                .stream()
                .map(ClientAuthenticationMethod::getValue)
                .collect(Collectors.joining(","));
        oauth2Client.setAuthenticationMethods(authMethods);
        
        // Convert grant types to string
        String grantTypes = registeredClient.getAuthorizationGrantTypes()
                .stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.joining(","));
        oauth2Client.setAuthorizationGrantTypes(grantTypes);
        
        // Convert redirect URIs to string
        String redirectUris = String.join(",", registeredClient.getRedirectUris());
        oauth2Client.setRedirectUris(redirectUris);
        
        // Convert scopes to string
        String scopes = String.join(",", registeredClient.getScopes());
        oauth2Client.setScopes(scopes);
        
        // Set token TTL values
        TokenSettings tokenSettings = registeredClient.getTokenSettings();
        oauth2Client.setAccessTokenTtl((int) tokenSettings.getAccessTokenTimeToLive().toSeconds());
        oauth2Client.setRefreshTokenTtl((int) tokenSettings.getRefreshTokenTimeToLive().toSeconds());
        
        oauth2ClientRepository.save(oauth2Client);
        log.info("Saved OAuth2 client: {}", registeredClient.getClientId());
    }

    @Override
    public RegisteredClient findById(String id) {
        try {
            Long entityId = Long.parseLong(id);
            return oauth2ClientRepository.findById(entityId)
                    .map(this::toRegisteredClient)
                    .orElse(null);
        } catch (NumberFormatException e) {
            log.warn("Invalid ID format for OAuth2 client: {}", id);
            return null;
        }
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return oauth2ClientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient toRegisteredClient(Oauth2Client oauth2Client) {
        // Parse authentication methods
        Set<ClientAuthenticationMethod> authMethods = Arrays.stream(
                oauth2Client.getAuthenticationMethods().split(","))
                .map(String::trim)
                .map(ClientAuthenticationMethod::new)
                .collect(Collectors.toSet());
        
        // Parse grant types
        Set<AuthorizationGrantType> grantTypes = Arrays.stream(
                oauth2Client.getAuthorizationGrantTypes().split(","))
                .map(String::trim)
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toSet());
        
        // Parse redirect URIs
        Set<String> redirectUris = Arrays.stream(
                oauth2Client.getRedirectUris().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        
        // Parse scopes
        Set<String> scopes = Arrays.stream(
                oauth2Client.getScopes().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        
        return RegisteredClient.withId(oauth2Client.getId().toString())
                .clientId(oauth2Client.getClientId())
                .clientSecret(oauth2Client.getClientSecret())
                .clientName(oauth2Client.getClientName())
                .clientAuthenticationMethods(methods -> methods.addAll(authMethods))
                .authorizationGrantTypes(grants -> grants.addAll(grantTypes))
                .redirectUris(uris -> uris.addAll(redirectUris))
                .scopes(scopeSet -> scopeSet.addAll(scopes))
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofSeconds(oauth2Client.getAccessTokenTtl()))
                        .refreshTokenTimeToLive(Duration.ofSeconds(oauth2Client.getRefreshTokenTtl()))
                        .reuseRefreshTokens(false) // Enable refresh token rotation
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true) // Enable PKCE for security
                        .build())
                .build();
    }
}