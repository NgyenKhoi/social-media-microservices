package com.nguyenkhoi.auth_service.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private Boolean isEnabled;
    private Boolean isLocked;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> roles;
}