package com.nguyenkhoi.auth_service.repository;

import com.nguyenkhoi.auth_service.entities.UserExternalAccount;
import com.nguyenkhoi.auth_service.entities.UserExternalAccount.OAuthProvider;
import com.nguyenkhoi.auth_service.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserExternalAccountRepository extends JpaRepository<UserExternalAccount, Long> {
    
    Optional<UserExternalAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
    
    Optional<UserExternalAccount> findByUserAndProvider(AppUser user, OAuthProvider provider);
    
    Optional<UserExternalAccount> findByUserIdAndProvider(UUID userId, OAuthProvider provider);
    
    List<UserExternalAccount> findByUser(AppUser user);
    
    List<UserExternalAccount> findByUserId(UUID userId);
    
    List<UserExternalAccount> findByProvider(OAuthProvider provider);
    
    @Query("SELECT uea FROM UserExternalAccount uea WHERE uea.providerEmail = :email AND uea.provider = :provider")
    Optional<UserExternalAccount> findByProviderEmail(@Param("email") String email, @Param("provider") OAuthProvider provider);
    
    boolean existsByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
    
    boolean existsByUserAndProvider(AppUser user, OAuthProvider provider);
}