package com.nguyenkhoi.auth_service.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class SessionResponse {
    private UUID id;
    private String deviceInfo;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    private Instant lastActive;
    private Boolean revoked;
}