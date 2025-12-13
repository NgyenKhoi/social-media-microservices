package com.nguyenkhoi.auth_service.utils;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class PemUtils {

    public static RSAPrivateKey parseRSAPrivateKey(String pem) {
        try {
            String privateKey = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA private key", e);
        }
    }
}
