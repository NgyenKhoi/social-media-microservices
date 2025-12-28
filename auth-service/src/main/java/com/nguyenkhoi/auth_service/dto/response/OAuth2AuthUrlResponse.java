package com.nguyenkhoi.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AuthUrlResponse {
    
    private String authUrl;
    private String state;
    private String provider;
}