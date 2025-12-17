package com.nguyenkhoi.auth_service.dto.response;

import lombok.Data;
import java.time.Instant;

@Data
public class TokenResponse {
    private String token;
    private String chainId;
    private Instant issuedAt;
    private Instant expiryAt;
    private Boolean revoked;
}