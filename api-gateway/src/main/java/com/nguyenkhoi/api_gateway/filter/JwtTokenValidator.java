package com.nguyenkhoi.api_gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import com.nguyenkhoi.api_gateway.config.GatewayProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
@Service
public class JwtTokenValidator {

    @Value("${jwt.public-key-path:classpath:jwt_public.pem}")
    private Resource publicKeyResource;
    
    private final GatewayProperties gatewayProperties;
    private PublicKey publicKey;

    public JwtTokenValidator(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @PostConstruct
    public void initializePublicKey() {
        try {
            this.publicKey = loadPublicKeyFromResource();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JWT public key", e);
        }
    }

    public Claims validateToken(String token) throws JwtException {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException("Token cannot be null or empty");
        }

        Claims claims = Jwts.parser()
            .verifyWith(publicKey)
            .requireIssuer(gatewayProperties.getJwt().getIssuer())
            .requireAudience(gatewayProperties.getJwt().getAudience())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        validateTokenExpiration(claims);
        return claims;
    }

    private void validateTokenExpiration(Claims claims) throws JwtException {
        if (claims.getExpiration() != null && claims.getExpiration().before(new java.util.Date())) {
            throw new JwtException("Token has expired");
        }
    }

    private PublicKey loadPublicKeyFromResource() throws Exception {
        String publicKeyContent = StreamUtils.copyToString(
            publicKeyResource.getInputStream(), 
            StandardCharsets.UTF_8
        );
        
        // Clean the PEM format
        publicKeyContent = publicKeyContent
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        return keyFactory.generatePublic(spec);
    }
}