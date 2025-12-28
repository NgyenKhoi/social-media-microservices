package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.dto.response.SessionResponse;
import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.entities.UserSession;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import com.nguyenkhoi.auth_service.mapper.SessionMapper;
import com.nguyenkhoi.auth_service.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {

    private final UserSessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    @Value("${security.max-sessions-per-user}")
    private int maxSessionsPerUser;

    @Value("${security.session-timeout-hours}")
    private int sessionTimeoutHours;

    @Transactional
    public UserSession createSession(AppUser user, String ipAddress, String userAgent, String deviceInfo) {
        cleanupExcessiveSessions(user.getId());

        UserSession session = new UserSession();
        session.setUser(user);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceInfo(deviceInfo);
        session.setRevoked(false);
        session.setLastActive(Instant.now());

        return sessionRepository.save(session);
    }

    @Transactional
    public String createSession(AppUser user, String userAgent, String ipAddress) {
        String deviceInfo = extractDeviceInfo(userAgent);
        UserSession session = createSession(user, ipAddress, userAgent, deviceInfo);
        return session.getId().toString();
    }

    public Optional<SessionResponse> findById(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .map(sessionMapper::toResponse);
    }

    @Transactional
    public void updateLastActive(UUID sessionId) {
        sessionRepository.updateLastActive(sessionId, Instant.now());
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.revokeSession(sessionId);
    }

    @Transactional
    public void revokeAllUserSessions(UUID userId) {
        sessionRepository.revokeAllUserSessions(userId);
    }

    public List<SessionResponse> getActiveSessionsForUser(UUID userId) {
        List<UserSession> sessions = sessionRepository.findActiveSessionsByUserId(userId);
        return sessionMapper.toResponseList(sessions);
    }

    public List<SessionResponse> getActiveSessionsForUserAndIp(UUID userId, String ipAddress) {
        List<UserSession> sessions = sessionRepository.findActiveSessionsByUserAndIp(userId, ipAddress);
        return sessionMapper.toResponseList(sessions);
    }

    public long countActiveSessionsForUser(UUID userId) {
        return sessionRepository.countActiveSessionsByUserId(userId);
    }

    public boolean isSessionValid(UserSession session) {
        if (session == null || session.getRevoked()) {
            return false;
        }

        Instant expirationThreshold = Instant.now().minus(sessionTimeoutHours, ChronoUnit.HOURS);
        return session.getLastActive().isAfter(expirationThreshold);
    }

    @Transactional
    public void cleanupInactiveSessions() {
        Instant threshold = Instant.now().minus(sessionTimeoutHours, ChronoUnit.HOURS);
        List<UserSession> inactiveSessions = sessionRepository.findInactiveSessions(threshold);
        
        for (UserSession session : inactiveSessions) {
            session.setRevoked(true);
            sessionRepository.save(session);
        }
    }

    @Transactional
    public void cleanupExcessiveSessions(UUID userId) {
        long activeSessionCount = sessionRepository.countActiveSessionsByUserId(userId);
        
        if (activeSessionCount >= maxSessionsPerUser) {
            List<UserSession> activeSessions = sessionRepository.findActiveSessionsByUserId(userId);
            
            activeSessions.stream()
                    .sorted((s1, s2) -> s1.getLastActive().compareTo(s2.getLastActive()))
                    .limit(activeSessionCount - maxSessionsPerUser + 1)
                    .forEach(session -> {
                        session.setRevoked(true);
                        sessionRepository.save(session);
                    });
        }
    }

    public SessionResponse getSessionInfo(UUID sessionId) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));
        return sessionMapper.toResponse(session);
    }

    public boolean detectSuspiciousActivity(UUID userId) {
        List<UserSession> activeSessions = sessionRepository.findActiveSessionsByUserId(userId);
        
        long uniqueIpCount = activeSessions.stream()
                .map(UserSession::getIpAddress)
                .distinct()
                .count();
        
        return uniqueIpCount > 3;
    }

    public String extractDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return "Unknown Device";
        }

        String lowerUserAgent = userAgent.toLowerCase();
        
        if (lowerUserAgent.contains("mobile") || lowerUserAgent.contains("android") || lowerUserAgent.contains("iphone")) {
            return "Mobile Device";
        } else if (lowerUserAgent.contains("tablet") || lowerUserAgent.contains("ipad")) {
            return "Tablet";
        } else if (lowerUserAgent.contains("windows")) {
            return "Windows Desktop";
        } else if (lowerUserAgent.contains("mac")) {
            return "Mac Desktop";
        } else if (lowerUserAgent.contains("linux")) {
            return "Linux Desktop";
        } else {
            return "Desktop Browser";
        }
    }
}