package com.example.oauth.service.impl;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.TokenNotFoundException;
import com.example.oauth.model.OAuthToken;
import com.example.oauth.model.TokenResponse;
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
import java.time.LocalDateTime;

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
        String baseUrl = config.getAuthorizationUrl();
        log.info("Iniciando geração de URL para autorização na HubSpot. baseUrl: [{}]", baseUrl);
        String encodedRedirectUri = URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8);
        String encodedScopes = URLEncoder.encode(config.getScopes(), StandardCharsets.UTF_8);

        return String.format(
                "%s?client_id=%s&redirect_uri=%s&scope=%s",
                baseUrl,
                config.getClientId(),
                encodedRedirectUri,
                encodedScopes
        );
    }

    @Override
    public String exchangeCodeForToken(String code) {
        String body = getBody(code);

        try {
            HttpResponse<String> response = HttpUtil.postForm(config.getTokenUrl(), body);

            if (HttpStatusCode.valueOf(response.statusCode()).is2xxSuccessful()) {
                log.info("Resposta do HubSpot para gerar token, retornou status: [{}]", response.statusCode());
                TokenResponse tokenResponse = objectMapper.readValue(response.body(), TokenResponse.class);

                OAuthToken token = new OAuthToken();
                token.setAccessToken(tokenResponse.getAccessToken());
                token.setRefreshToken(tokenResponse.getRefreshToken());
                token.setExpiresIn(tokenResponse.getExpiresIn());
                tokenManager.saveToken(token);
                return "Token salvo com sucesso!";
            } else {
                log.error("Erro ao trocar o código por token, Status: [{}], Body: [{}]", response.statusCode(), response.body());
                throw new RuntimeException("Erro ao trocar o código por token");
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Erro ao trocar o código por token: " + e.getMessage(), e);
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
        log.info("Iniciando validação do token HubSpot para criação de contato");
        return tokenManager.getValidToken();
    }



}
