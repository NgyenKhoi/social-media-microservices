package com.nguyenkhoi.auth_service.mapper;

import com.nguyenkhoi.auth_service.dto.response.SessionResponse;
import com.nguyenkhoi.auth_service.entities.UserSession;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    
    SessionResponse toResponse(UserSession session);
    
    List<SessionResponse> toResponseList(List<UserSession> sessions);
}