package com.nguyenkhoi.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private Security security = new Security();
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();

    public static class Security {
        private List<String> openEndpoints = List.of(
            "/auth/login",
            "/auth/register", 
            "/auth/refresh-token",
            "/auth/oauth2/callback/google",
            "/actuator/health"
        );
        
        private boolean enableRateLimiting = true;
        private int rateLimitPerMinute = 100;

        public List<String> getOpenEndpoints() { return openEndpoints; }
        public void setOpenEndpoints(List<String> openEndpoints) { this.openEndpoints = openEndpoints; }
        
        public boolean isEnableRateLimiting() { return enableRateLimiting; }
        public void setEnableRateLimiting(boolean enableRateLimiting) { this.enableRateLimiting = enableRateLimiting; }
        
        public int getRateLimitPerMinute() { return rateLimitPerMinute; }
        public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    }

    public static class Jwt {
        private String publicKeyPath = "classpath:jwt_public.pem";
        private String issuer = "auth-service";
        private String audience = "social-media-platform";

        public String getPublicKeyPath() { return publicKeyPath; }
        public void setPublicKeyPath(String publicKeyPath) { this.publicKeyPath = publicKeyPath; }
        
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        
        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of(
            "http://localhost:3000",
            "http://localhost:5173"
        );
        private List<String> allowedMethods = List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        );
        private boolean allowCredentials = true;
        private long maxAge = 3600L;

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        
        public List<String> getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }
        
        public boolean isAllowCredentials() { return allowCredentials; }
        public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
        
        public long getMaxAge() { return maxAge; }
        public void setMaxAge(long maxAge) { this.maxAge = maxAge; }
    }

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
}