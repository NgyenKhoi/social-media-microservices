package com.nguyenkhoi.auth_service.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http
            .exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(
                    new LoginUrlAuthenticationEntryPoint("/login")
                )
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/oauth2/token",
                "/.well-known/openid-configuration",
                "/.well-known/jwks.json"
            ))
            .headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            );

        return http.build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        if (rsaKey == null) {
            throw new AppException(
                ErrorCode.JWT_EXCEPTION,
                "RSAKey bean not found"
            );
        }
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${spring.security.oauth2.authorizationserver.issuer}") String issuer) {

        if (issuer == null || issuer.isBlank()) {
            throw new AppException(
                ErrorCode.JWT_EXCEPTION,
                "Issuer is not configured"
            );
        }

        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }
}