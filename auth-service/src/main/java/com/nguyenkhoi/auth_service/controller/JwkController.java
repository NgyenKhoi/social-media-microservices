package com.nguyenkhoi.auth_service.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JwkController {

    private final JWKSource<SecurityContext> jwkSource;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwkSet() {
        try {
            JWKSet jwkSet = new JWKSet(jwkSource.get(null, null));
            return jwkSet.toJSONObject();
        } catch (Exception e) {
            log.error("Error retrieving JWK set", e);
            throw new RuntimeException("Unable to retrieve JWK set", e);
        }
    }
}