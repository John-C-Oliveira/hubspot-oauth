package com.example.oauth.service;

import com.example.oauth.repository.OAuthToken;

public interface OAuthService {

    String getAuthorizationUrl();

    String exchangeCodeForToken(String code);

    OAuthToken getValidTokenSync();
}
