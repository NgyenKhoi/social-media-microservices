package com.nguyenkhoi.auth_service.config;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.math.BigInteger;

import com.nguyenkhoi.auth_service.utils.PemUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtKeyConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${jwt.key.private-path}")
    private String privateKeyPath;

    @Bean
    public RSAKey rsaKey() {
        String privateKeyPem = loadPrivateKeyFromPath(privateKeyPath);
        RSAPrivateKey privateKey = PemUtils.parseRSAPrivateKey(privateKeyPem);
        RSAPublicKey publicKey = derivePublicKey(privateKey);

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("auth-service-rsa-1")
                .build();
    }

    private RSAPublicKey derivePublicKey(RSAPrivateKey privateKey) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(
                            privateKey.getModulus(),
                            BigInteger.valueOf(65537)
                    ));
        } catch (Exception e) {
            throw new AppException(ErrorCode.JWT_EXCEPTION, "Failed to derive public key from RSA private key");
        }
    }

     @Bean
    public RSAPublicKey jwtValidationKey(RSAKey rsaKey) {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.JWT_EXCEPTION, "Failed to extract RSA public key from RSAKey");
        }
    }

    @Bean
    public RSAPrivateKey jwtSigningKey(RSAKey rsaKey) {
        try {
            return rsaKey.toRSAPrivateKey();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.JWT_EXCEPTION, "Failed to extract RSA private key from RSAKey");
        }
    }

    private String loadPrivateKeyFromPath(String keyPath) {
        try {
            Resource resource = resourceLoader.getResource(keyPath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AppException(ErrorCode.JWT_EXCEPTION, "Failed to load private key from path: " + keyPath);
        }
    }
}
