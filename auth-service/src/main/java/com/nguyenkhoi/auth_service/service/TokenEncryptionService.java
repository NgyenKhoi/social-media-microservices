package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class TokenEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private final SecretKey secretKey;

    public TokenEncryptionService(@Value("${security.token.encryption.key:}") String encryptionKey) {
        this.secretKey = initializeSecretKey(encryptionKey);
    }

    private SecretKey initializeSecretKey(String encryptionKey) {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            // Generate a random key for development/testing
            log.warn("No encryption key provided, generating random key for development");
            return generateRandomKey();
        } else {
            // Use provided key (should be base64 encoded)
            try {
                byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
                return new SecretKeySpec(keyBytes, ALGORITHM);
            } catch (Exception e) {
                log.error("Invalid encryption key provided, generating random key", e);
                return generateRandomKey();
            }
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            log.error("Error encrypting token", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Token encryption failed");
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            System.arraycopy(encryptedWithIv, iv.length, encryptedData, 0, encryptedData.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting token", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Token decryption failed");
        }
    }

    private SecretKey generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to generate encryption key");
        }
    }

    public String generateBase64Key() {
        SecretKey key = generateRandomKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}