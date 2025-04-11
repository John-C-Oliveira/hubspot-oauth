package com.example.oauth.repository;

import com.example.oauth.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findTopByOrderByCreatedAtDesc();
}