package com.nguyenkhoi.auth_service.repository;

import com.nguyenkhoi.auth_service.entities.Oauth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Oauth2ClientRepository extends JpaRepository<Oauth2Client, Long> {
    
    Optional<Oauth2Client> findByClientId(String clientId);
    
    boolean existsByClientId(String clientId);
}