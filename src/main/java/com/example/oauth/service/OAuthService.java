package com.example.oauth.service;

import com.example.oauth.model.OAuthToken;

public interface OAuthService {

    String getAuthorizationUrl();

    String exchangeCodeForToken(String code);

    OAuthToken getValidTokenSync();
}
