package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.dto.response.TokenResponse;
import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.entities.RefreshToken;
import com.nguyenkhoi.auth_service.entities.UserSession;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import com.nguyenkhoi.auth_service.mapper.TokenMapper;
import com.nguyenkhoi.auth_service.repository.RefreshTokenRepository;
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
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final TokenMapper tokenMapper;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${security.max-refresh-tokens-per-user}")
    private int maxRefreshTokensPerUser;

    @Transactional
    public TokenResponse createRefreshToken(AppUser user, UserSession session, String chainId, String ipAddress, String userAgent) {
        cleanupExcessiveTokens(user.getId());

        String tokenIdentifier = jwtTokenService.generateRefreshTokenIdentifier();
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setSession(session);
        refreshToken.setToken(tokenIdentifier);
        refreshToken.setChainId(chainId);
        refreshToken.setIssuedAt(now);
        refreshToken.setExpiryAt(expiration);
        refreshToken.setRevoked(false);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        return tokenMapper.toResponse(savedToken);
    }

    @Transactional
    public TokenResponse rotateRefreshToken(String oldTokenIdentifier, String ipAddress, String userAgent) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenIdentifier)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (oldToken.getRevoked()) {
            revokeTokenChain(oldToken.getChainId());
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (oldToken.getExpiryAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        TokenResponse newToken = createRefreshToken(
                oldToken.getUser(),
                oldToken.getSession(),
                oldToken.getChainId(),
                ipAddress,
                userAgent
        );

        oldToken.setRevoked(true);
        oldToken.setReplacedBy(newToken.getToken());
        refreshTokenRepository.save(oldToken);

        return newToken;
    }

    public RefreshToken validateRefreshToken(String tokenIdentifier) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenIdentifier)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (token.getRevoked()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (token.getExpiryAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        return token;
    }

    @Transactional
    public void revokeRefreshToken(String tokenIdentifier) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(tokenIdentifier);
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }
    }

    @Transactional
    public void revokeTokenChain(String chainId) {
        refreshTokenRepository.revokeTokensByChainId(chainId);
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    public List<TokenResponse> getActiveTokensForUser(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findActiveTokensByUserId(userId);
        return tokenMapper.toResponseList(tokens);
    }

    public List<TokenResponse> getActiveTokensForChain(String chainId) {
        List<RefreshToken> tokens = refreshTokenRepository.findActiveTokensByChainId(chainId);
        return tokenMapper.toResponseList(tokens);
    }

    public long countActiveTokensForUser(UUID userId) {
        return refreshTokenRepository.countActiveTokensByUserId(userId);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }

    @Transactional
    public void cleanupExcessiveTokens(UUID userId) {
        long activeTokenCount = refreshTokenRepository.countActiveTokensByUserId(userId);
        
        if (activeTokenCount >= maxRefreshTokensPerUser) {
            List<RefreshToken> activeTokens = refreshTokenRepository.findActiveTokensByUserId(userId);
            
            activeTokens.stream()
                    .sorted((t1, t2) -> t1.getIssuedAt().compareTo(t2.getIssuedAt()))
                    .limit(activeTokenCount - maxRefreshTokensPerUser + 1)
                    .forEach(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
    }

    public Optional<RefreshToken> findByToken(String tokenIdentifier) {
        return refreshTokenRepository.findByToken(tokenIdentifier);
    }

    public boolean isValidRefreshToken(String tokenIdentifier) {
        try {
            validateRefreshToken(tokenIdentifier);
            return true;
        } catch (AppException e) {
            return false;
        }
    }
}