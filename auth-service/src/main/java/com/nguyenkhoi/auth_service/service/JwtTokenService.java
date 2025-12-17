package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.entities.UserRole;
import com.nguyenkhoi.auth_service.entities.UserSession;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {

    private final RSAPrivateKey jwtSigningKey;
    private final RSAPublicKey jwtValidationKey;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    public String generateAccessToken(AppUser user, UserSession session, String chainId) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

            List<String> roles = user.getRoles().stream()
                    .map(UserRole::getName)
                    .collect(Collectors.toList());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .issuer(issuer)
                    .audience(audience)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .claim("roles", roles)
                    .claim("session_id", session.getId().toString())
                    .claim("chain_id", chainId)
                    .claim("token_type", "access")
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID("auth-service-rsa-1")
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new RSASSASigner(jwtSigningKey));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.JWT_EXCEPTION);
        }
    }

    public String generateRefreshTokenIdentifier() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String generateChainId() {
        return UUID.randomUUID().toString();
    }

    public JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            RSASSAVerifier verifier = new RSASSAVerifier(jwtValidationKey);
            if (!signedJWT.verify(verifier)) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            
            if (!issuer.equals(claims.getIssuer())) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            
            if (!claims.getAudience().contains(audience)) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            
            if (claims.getExpirationTime().before(new Date())) {
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }

            return claims;
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public UUID extractUserId(JWTClaimsSet claims) {
        try {
            return UUID.fromString(claims.getSubject());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public UUID extractSessionId(JWTClaimsSet claims) {
        try {
            String sessionId = claims.getStringClaim("session_id");
            return sessionId != null ? UUID.fromString(sessionId) : null;
        } catch (IllegalArgumentException | ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String extractChainId(JWTClaimsSet claims) {
        try {
            return claims.getStringClaim("chain_id");
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String extractEmail(JWTClaimsSet claims) {
        try {
            return claims.getStringClaim("email");
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(JWTClaimsSet claims) {
        Object rolesObj = claims.getClaim("roles");
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        return List.of();
    }

    public String extractTokenType(JWTClaimsSet claims) {
        try {
            return claims.getStringClaim("token_type");
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public boolean isTokenExpired(JWTClaimsSet claims) {
        return claims.getExpirationTime().before(new Date());
    }

    public Instant getTokenExpiration(JWTClaimsSet claims) {
        return claims.getExpirationTime().toInstant();
    }

    public JWTClaimsSet parseTokenWithoutValidation(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}