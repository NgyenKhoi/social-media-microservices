package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.entities.UserExternalAccount;
import com.nguyenkhoi.auth_service.entities.UserExternalAccount.OAuthProvider;
import com.nguyenkhoi.auth_service.entities.UserRole;
import com.nguyenkhoi.auth_service.exception.AppException;
import com.nguyenkhoi.auth_service.exception.ErrorCode;
import com.nguyenkhoi.auth_service.repository.AppUserRepository;
import com.nguyenkhoi.auth_service.repository.UserExternalAccountRepository;
import com.nguyenkhoi.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService {

    private final AppUserRepository userRepository;
    private final UserExternalAccountRepository externalAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final TokenEncryptionService tokenEncryptionService;

    @Transactional
    public AppUser processOAuth2User(OAuthProvider provider, String providerUserId, 
                                   String providerEmail, String accessToken, 
                                   String refreshToken, Long expiresIn, String scopes) {
        Optional<UserExternalAccount> existingAccount = externalAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId);

        if (existingAccount.isPresent()) {
            UserExternalAccount account = existingAccount.get();
            updateExternalAccountTokens(account, accessToken, refreshToken, expiresIn, scopes);
            return account.getUser();
        }

        Optional<AppUser> existingUser = userRepository.findByEmail(providerEmail);
        
        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();
            createExternalAccount(user, provider, providerUserId, providerEmail, 
                                accessToken, refreshToken, expiresIn, scopes);
            return user;
        }

        AppUser newUser = createUserFromOAuth(provider, providerUserId, providerEmail);
        createExternalAccount(newUser, provider, providerUserId, providerEmail, 
                            accessToken, refreshToken, expiresIn, scopes);
        
        return newUser;
    }

    @Transactional
    public AppUser createUserFromOAuth(OAuthProvider provider, String providerUserId, String providerEmail) {
        String username = generateUniqueUsername(providerEmail, provider, providerUserId);

        AppUser user = new AppUser();
        user.setEmail(providerEmail);
        user.setUsername(username);
        user.setPassword(null);
        user.setIsEnabled(true);
        user.setIsLocked(false);

        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    @Transactional
    public UserExternalAccount createExternalAccount(AppUser user, OAuthProvider provider, 
                                                   String providerUserId, String providerEmail,
                                                   String accessToken, String refreshToken, 
                                                   Long expiresIn, String scopes) {
        if (externalAccountRepository.existsByUserAndProvider(user, provider)) {
            throw new AppException(ErrorCode.OAUTH_ACCOUNT_ALREADY_LINKED);
        }

        UserExternalAccount account = new UserExternalAccount();
        account.setUser(user);
        account.setProvider(provider);
        account.setProviderUserId(providerUserId);
        account.setProviderEmail(providerEmail);
        account.setAccessToken(encryptToken(accessToken));
        account.setRefreshToken(encryptToken(refreshToken));
        account.setScopes(scopes);

        if (expiresIn != null) {
            account.setTokenExpiry(Instant.now().plus(expiresIn, ChronoUnit.SECONDS));
        }

        return externalAccountRepository.save(account);
    }

    @Transactional
    public void updateExternalAccountTokens(UserExternalAccount account, String accessToken, 
                                          String refreshToken, Long expiresIn, String scopes) {
        account.setAccessToken(encryptToken(accessToken));
        if (refreshToken != null) {
            account.setRefreshToken(encryptToken(refreshToken));
        }
        if (scopes != null) {
            account.setScopes(scopes);
        }
        if (expiresIn != null) {
            account.setTokenExpiry(Instant.now().plus(expiresIn, ChronoUnit.SECONDS));
        }

        externalAccountRepository.save(account);
    }

    public Optional<AppUser> findUserByOAuth(OAuthProvider provider, String providerUserId) {
        return externalAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(UserExternalAccount::getUser);
    }

    public Optional<UserExternalAccount> findExternalAccount(AppUser user, OAuthProvider provider) {
        return externalAccountRepository.findByUserAndProvider(user, provider);
    }

    public boolean hasLinkedAccount(AppUser user, OAuthProvider provider) {
        return externalAccountRepository.existsByUserAndProvider(user, provider);
    }

    @Transactional
    public void unlinkOAuthAccount(AppUser user, OAuthProvider provider) {
        Optional<UserExternalAccount> account = externalAccountRepository.findByUserAndProvider(user, provider);
        if (account.isPresent()) {
            externalAccountRepository.delete(account.get());
        } else {
            throw new AppException(ErrorCode.OAUTH_ACCOUNT_NOT_FOUND);
        }
    }

    private String generateUniqueUsername(String email, OAuthProvider provider, String providerUserId) {
        String baseUsername;
        
        if (email != null && email.contains("@")) {
            baseUsername = email.substring(0, email.indexOf("@"));
        } else {
            baseUsername = provider.name().toLowerCase() + "_" + providerUserId;
        }

        baseUsername = baseUsername.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();

        if (baseUsername.isEmpty()) {
            baseUsername = provider.name().toLowerCase() + "_" + providerUserId.replaceAll("[^a-zA-Z0-9_]", "");
        }

        if (baseUsername.length() > 30) {
            baseUsername = baseUsername.substring(0, 30);
        }

        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
                username = baseUsername + "_" + counter;
                counter++;

                if (counter > 10000) {
                    username = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 8);
                    while (userRepository.existsByUsername(username)) {
                        username = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 8);
                    }
                    break;
                }
            }
            return username;
        }

    private String encryptToken(String token) {
        if (token == null) {
            return null;
        }
        return tokenEncryptionService.encrypt(token);
    }

    private String decryptToken(String encryptedToken) {
        if (encryptedToken == null) {
            return null;
        }
        return tokenEncryptionService.decrypt(encryptedToken);
    }

    public boolean needsTokenRefresh(UserExternalAccount account) {
        if (account.getTokenExpiry() == null) {
            return false;
        }
        
        return account.getTokenExpiry().isBefore(Instant.now().plus(5, ChronoUnit.MINUTES));
    }
}