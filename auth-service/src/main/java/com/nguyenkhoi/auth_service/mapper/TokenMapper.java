package com.nguyenkhoi.auth_service.mapper;

import com.nguyenkhoi.auth_service.dto.response.TokenResponse;
import com.nguyenkhoi.auth_service.entities.RefreshToken;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TokenMapper {
    
    TokenResponse toResponse(RefreshToken token);
    
    List<TokenResponse> toResponseList(List<RefreshToken> tokens);
}