package com.nguyenkhoi.auth_service.repository;

import com.nguyenkhoi.auth_service.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    Optional<UserRole> findByName(String name);
    
    boolean existsByName(String name);
}