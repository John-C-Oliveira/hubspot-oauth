package com.example.oauth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class HubSpotOAuthConfig {

    @Value("${hubspot.client-id}")
    private String clientId;

    @Value("${hubspot.client-secret}")
    private String clientSecret;

    @Value("${hubspot.redirect-uri}")
    private String redirectUri;

    @Value("${hubspot.scopes}")
    private String scopes;

    @Value("${hubspot.token-url}")
    private String tokenUrl;

    @Value("${hubspot.authorization-url}")
    private String authorizationUrl;

    @Value("${hubspot.creation-contact}")
    private String creationContact;

}