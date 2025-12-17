package com.nguyenkhoi.auth_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_external_account", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_provider_user", columnNames = {"provider", "provider_user_id"}),
        @UniqueConstraint(name = "uk_user_provider", columnNames = {"user_id", "provider"})
    },
    indexes = {
        @Index(name = "idx_external_provider", columnList = "provider"),
        @Index(name = "idx_external_user", columnList = "user_id")
    }
)
public class UserExternalAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "provider_email", length = 100)
    private String providerEmail;

    @Column(name = "access_token", length = Integer.MAX_VALUE)
    private String accessToken; // Should be encrypted in production

    @Column(name = "refresh_token", length = Integer.MAX_VALUE)
    private String refreshToken; // Should be encrypted in production

    @Column(name = "token_expiry")
    private Instant tokenExpiry;

    @Column(name = "scopes", length = Integer.MAX_VALUE)
    private String scopes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum OAuthProvider {
        GOOGLE, FACEBOOK, GITHUB, TWITTER
    }
}