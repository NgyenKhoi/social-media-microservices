package com.nguyenkhoi.auth_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "oauth2_client", indexes = {
    @Index(name = "idx_oauth2_client_id", columnList = "client_id")
})
public class Oauth2Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 100, unique = true)
    private String clientId;

    @Column(name = "client_secret", length = 255)
    private String clientSecret;

    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "authentication_methods", columnDefinition = "TEXT")
    private String authenticationMethods;

    @Column(name = "authorization_grant_types", columnDefinition = "TEXT")
    private String authorizationGrantTypes;

    @Column(name = "redirect_uris", columnDefinition = "TEXT")
    private String redirectUris;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "access_token_ttl")
    private Integer accessTokenTtl;

    @Column(name = "refresh_token_ttl")
    private Integer refreshTokenTtl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}