package com.nguyenkhoi.auth_service.service;

import com.nguyenkhoi.auth_service.entities.AppUser;
import com.nguyenkhoi.auth_service.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        // Try to find by username first, then by email
        AppUser appUser = appUserRepository.findByUsername(username)
                .or(() -> appUserRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword() != null ? appUser.getPassword() : "")
                .authorities(getAuthorities(appUser))
                .accountExpired(false)
                .accountLocked(appUser.getIsLocked())
                .credentialsExpired(false)
                .disabled(!appUser.getIsEnabled())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(AppUser appUser) {
        // For now, assign a default role. This can be enhanced later with proper role management
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}