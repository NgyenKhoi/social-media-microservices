package com.nguyenkhoi.auth_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_session", indexes = {
    @Index(name = "idx_session_user", columnList = "user_id"),
    @Index(name = "idx_session_active", columnList = "last_active")
})
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "device_info", length = Integer.MAX_VALUE)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_active", nullable = false)
    private Instant lastActive = Instant.now();

    @ColumnDefault("false")
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RefreshToken> refreshTokens;

    @PrePersist
    @PreUpdate
    public void updateLastActive() {
        this.lastActive = Instant.now();
    }
}