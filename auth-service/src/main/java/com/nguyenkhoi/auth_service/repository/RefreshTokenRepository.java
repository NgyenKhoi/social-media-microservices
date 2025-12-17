package com.nguyenkhoi.auth_service.repository;

import com.nguyenkhoi.auth_service.entities.RefreshToken;
import com.nguyenkhoi.auth_service.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByChainId(String chainId);
    
    List<RefreshToken> findByUser(AppUser user);
    
    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.chainId = :chainId AND rt.revoked = false")
    List<RefreshToken> findActiveTokensByChainId(@Param("chainId") String chainId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.chainId = :chainId")
    int revokeTokensByChainId(@Param("chainId") String chainId);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    int revokeAllUserTokens(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false")
    long countActiveTokensByUserId(@Param("userId") UUID userId);
}