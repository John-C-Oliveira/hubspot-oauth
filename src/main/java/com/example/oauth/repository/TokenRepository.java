package com.example.oauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findTopByOrderByCreatedAtDesc();
}