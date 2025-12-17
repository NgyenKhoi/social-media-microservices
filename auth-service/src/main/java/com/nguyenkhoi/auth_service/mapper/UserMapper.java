package com.nguyenkhoi.auth_service.mapper;

import com.nguyenkhoi.auth_service.dto.response.UserResponse;
import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.entities.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "roles", source = "roles")
    UserResponse toResponse(AppUser user);
    
    List<UserResponse> toResponseList(List<AppUser> users);
    
    default List<String> mapRoles(Set<UserRole> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(UserRole::getName)
                .collect(Collectors.toList());
    }
}