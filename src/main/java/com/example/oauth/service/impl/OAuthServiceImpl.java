package com.example.oauth.service.impl;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.HubspotIntegrationException;
import com.example.oauth.model.TokenResponseDTO;
import com.example.oauth.repository.OAuthToken;
import com.example.oauth.service.OAuthService;
import com.example.oauth.util.HttpUtil;
import com.example.oauth.util.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static com.example.oauth.service.OAuthServiceConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final HubSpotOAuthConfig config;
    private final ObjectMapper objectMapper;
    private final TokenManager tokenManager;

    @Override
    public String getAuthorizationUrl() {
        String encodedRedirectUri = URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8);
        String encodedScopes = URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8);

        return String.format(
                "%s?client_id=%s&redirect_uri=%s&scope=%s",
                config.getAuthorizationUrl(),
                config.getClientId(),
                encodedRedirectUri,
                encodedScopes
        );
    }

    @Override
    public String exchangeCodeForToken(String code) {
        String body = getBody(code);

        HttpResponse<String> response;
        try {
            response = HttpUtil.postForm(config.getTokenUrl(), body);
        } catch (IOException | InterruptedException e) {
            log.error("Erro ao fazer requisição para troca de token: {}", e.getMessage());
            throw new HubspotIntegrationException("Erro de comunicação com a HubSpot ao tentar trocar o código por token.");
        }

        if (!HttpStatusCode.valueOf(response.statusCode()).is2xxSuccessful()) {
            log.error("Erro ao trocar código por token. Status: [{}], Body: [{}]", response.statusCode(), response.body());
            throw new HubspotIntegrationException("HubSpot retornou erro ao tentar trocar código por token.");
        }

        try {
            TokenResponseDTO tokenResponse = objectMapper.readValue(response.body(), TokenResponseDTO.class);

            OAuthToken token = new OAuthToken();
            token.setAccessToken(tokenResponse.getAccessToken());
            token.setRefreshToken(tokenResponse.getRefreshToken());
            token.setExpiresIn(tokenResponse.getExpiresIn());

            tokenManager.saveToken(token);
            return "Token salvo com sucesso!";
        } catch (IOException e) {
            log.error("Erro ao interpretar resposta de token da HubSpot: {}", response.body());
            throw new HubspotIntegrationException("Resposta da HubSpot inválida ao trocar token.");
        }
    }

    private String getBody(String code) {
        return "grant_type=authorization_code" +
                CLIENT_ID + config.getClientId() +
                CLIENT_SECRET + config.getClientSecret() +
                REDIRECT_URI + config.getRedirectUri() +
                CODE + code;
    }

    @Override
    public OAuthToken getValidTokenSync() {
        log.info("Validando token para integração com a HubSpot.");
        return tokenManager.getValidToken();
    }
}
