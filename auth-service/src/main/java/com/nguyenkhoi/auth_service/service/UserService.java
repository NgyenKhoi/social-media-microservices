package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.dto.request.RegisterRequest;
import com.nguyenkhoi.auth_service.dto.response.UserResponse;
import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.entities.UserRole;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import com.nguyenkhoi.auth_service.mapper.UserMapper;
import com.nguyenkhoi.auth_service.repository.AppUserRepository;
import com.nguyenkhoi.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(hashPassword(request.getPassword()));
        user.setIsEnabled(true);
        user.setIsLocked(false);

        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.setRoles(Set.of(userRole));

        AppUser savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<AppUser> findActiveUserByEmail(String email) {
        return userRepository.findActiveUserByEmail(email);
    }

    public Optional<UserResponse> findByIdWithRoles(UUID userId) {
        return userRepository.findByIdWithRoles(userId)
                .map(userMapper::toResponse);
    }

    public Optional<UserResponse> findByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email)
                .map(userMapper::toResponse);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public void lockUser(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        user.setIsLocked(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        user.setIsEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        user.setIsEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void unlockUser(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        user.setIsLocked(false);
        userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}