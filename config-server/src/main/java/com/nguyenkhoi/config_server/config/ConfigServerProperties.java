package com.nguyenkhoi.config_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config-server")
public class ConfigServerProperties {
    
    private String encryptKey;
    private String adminPassword;
    private boolean encryptionEnabled = true;
    private String searchLocation = "classpath:/config-repo/";
    
    public String getEncryptKey() {
        return encryptKey;
    }
    
    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }
    
    public String getAdminPassword() {
        return adminPassword;
    }
    
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
    
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
    
    public void setEncryptionEnabled(boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }
    
    public String getSearchLocation() {
        return searchLocation;
    }
    
    public void setSearchLocation(String searchLocation) {
        this.searchLocation = searchLocation;
    }
}