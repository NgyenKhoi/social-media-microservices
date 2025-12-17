package com.nguyenkhoi.auth_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "revoked_token", indexes = {
    @Index(name = "idx_revoked_chain", columnList = "chain_id"),
    @Index(name = "idx_revoked_user", columnList = "user_id"),
    @Index(name = "idx_revoked_expiry", columnList = "expiry_at")
})
public class RevokedToken {
    @Id
    @Column(name = "jti", nullable = false, length = 255)
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "chain_id", nullable = false, length = 255)
    private String chainId;

    @CreationTimestamp
    @Column(name = "revoked_at", nullable = false, updatable = false)
    private Instant revokedAt;

    @Column(name = "expiry_at", nullable = false)
    private Instant expiryAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revocation_reason", length = 50)
    private RevocationReason revocationReason;

    public enum RevocationReason {
        LOGOUT, SECURITY_BREACH, EXPIRED, ADMIN_REVOKED, TOKEN_ROTATION, SUSPICIOUS_ACTIVITY
    }
}