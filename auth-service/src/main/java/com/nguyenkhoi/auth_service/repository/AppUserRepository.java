package com.nguyenkhoi.auth_service.repository;

import com.nguyenkhoi.auth_service.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    
    Optional<AppUser> findByEmail(String email);
    
    Optional<AppUser> findByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM AppUser u WHERE u.email = :email AND u.isEnabled = true AND u.isLocked = false")
    Optional<AppUser> findActiveUserByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM AppUser u WHERE u.username = :username AND u.isEnabled = true AND u.isLocked = false")
    Optional<AppUser> findActiveUserByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<AppUser> findByIdWithRoles(@Param("id") UUID id);
    
    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<AppUser> findByEmailWithRoles(@Param("email") String email);
}