package com.nguyenkhoi.auth_service.repository;

import com.nguyenkhoi.auth_service.entities.RevokedToken;
import com.nguyenkhoi.auth_service.entities.RevokedToken.RevocationReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
    
    boolean existsByJti(String jti);
    
    List<RevokedToken> findByChainId(String chainId);
    
    List<RevokedToken> findByUser_IdOrderByRevokedAtDesc(UUID userId);
    
    List<RevokedToken> findByRevocationReason(RevocationReason reason);
    
    @Query("SELECT rt FROM RevokedToken rt WHERE rt.chainId = :chainId ORDER BY rt.revokedAt DESC")
    List<RevokedToken> findByChainIdOrderByRevokedAtDesc(@Param("chainId") String chainId);
    
    @Modifying
    @Query("DELETE FROM RevokedToken rt WHERE rt.expiryAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
    
    @Query("SELECT COUNT(rt) FROM RevokedToken rt WHERE rt.user.id = :userId")
    long countRevokedTokensByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT rt FROM RevokedToken rt WHERE rt.revokedAt BETWEEN :start AND :end")
    List<RevokedToken> findTokensRevokedBetween(@Param("start") Instant start, @Param("end") Instant end);
}