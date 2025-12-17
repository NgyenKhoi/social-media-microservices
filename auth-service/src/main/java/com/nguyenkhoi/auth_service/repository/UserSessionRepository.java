package com.nguyenkhoi.auth_service.repository;

import com.nguyenkhoi.auth_service.entities.UserSession;
import com.nguyenkhoi.auth_service.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    
    List<UserSession> findByUser(AppUser user);
    
    List<UserSession> findByUserIdAndRevokedFalse(UUID userId);
    
    @Query("SELECT us FROM UserSession us WHERE us.user.id = :userId AND us.revoked = false ORDER BY us.lastActive DESC")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT us FROM UserSession us WHERE us.user.id = :userId AND us.ipAddress = :ipAddress AND us.revoked = false")
    List<UserSession> findActiveSessionsByUserAndIp(@Param("userId") UUID userId, @Param("ipAddress") String ipAddress);
    
    @Modifying
    @Query("UPDATE UserSession us SET us.revoked = true WHERE us.user.id = :userId")
    int revokeAllUserSessions(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE UserSession us SET us.revoked = true WHERE us.id = :sessionId")
    int revokeSession(@Param("sessionId") UUID sessionId);
    
    @Modifying
    @Query("UPDATE UserSession us SET us.lastActive = :now WHERE us.id = :sessionId")
    int updateLastActive(@Param("sessionId") UUID sessionId, @Param("now") Instant now);
    
    @Query("SELECT COUNT(us) FROM UserSession us WHERE us.user.id = :userId AND us.revoked = false")
    long countActiveSessionsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT us FROM UserSession us WHERE us.lastActive < :threshold AND us.revoked = false")
    List<UserSession> findInactiveSessions(@Param("threshold") Instant threshold);
}