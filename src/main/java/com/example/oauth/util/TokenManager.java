package com.example.oauth.util;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.TokenNotFoundException;
import com.example.oauth.exceptions.handlers.TokenRefreshException;
import com.example.oauth.model.TokenResponseDTO;
import com.example.oauth.repository.OAuthToken;
import com.example.oauth.repository.TokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.oauth.service.OAuthServiceConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenManager {

    private final TokenRepository tokenRepository;
    private final HubSpotOAuthConfig config;
    private final ObjectMapper objectMapper;

    private OAuthToken oAuthToken;

    public void saveToken(OAuthToken token) {
        log.info("Salvando token no banco de dados");
        tokenRepository.save(token);
        log.info("Token salvo com sucesso!");
    }

    public synchronized OAuthToken getValidToken() {
        if (oAuthToken != null && !isExpired(oAuthToken)) {
            return oAuthToken;
        }

        log.info("Token em cache ausente ou expirado. Buscando no banco de dados...");
        Optional<OAuthToken> latest = fetchLatestToken();

        if (latest.isEmpty()) {
            throw new TokenNotFoundException("Nenhum token encontrado no banco de dados.");
        }

        OAuthToken token = refreshIfExpired(latest.get());
        oAuthToken = token;
        return token;
    }

    private Optional<OAuthToken> fetchLatestToken() {
        return tokenRepository.findTopByOrderByCreatedAtDesc();
    }

    private OAuthToken refreshIfExpired(OAuthToken token) {
        if (!isExpired(token)) {
            return token;
        }

        log.info("Token expirado. Iniciando processo de renovação via refresh_token...");
        return refreshToken(token);
    }

    private boolean isExpired(OAuthToken token) {
        return token.getCreatedAt().plusSeconds(token.getExpiresIn()).isBefore(LocalDateTime.now());
    }

    private OAuthToken refreshToken(OAuthToken token) {
        String body = "grant_type=refresh_token"
                + CLIENT_ID + config.getClientId()
                + CLIENT_SECRET + config.getClientSecret()
                + REFRESH_TOKEN + token.getRefreshToken();

        try {
            HttpResponse<String> response = HttpUtil.postForm(config.getTokenUrl(), body);

            if (HttpStatusCode.valueOf(response.statusCode()).is2xxSuccessful()) {
                TokenResponseDTO refreshed = objectMapper.readValue(response.body(), TokenResponseDTO.class);
                token.setAccessToken(refreshed.getAccessToken());
                token.setExpiresIn(refreshed.getExpiresIn());
                token.setCreatedAt(LocalDateTime.now());
                tokenRepository.save(token);

                log.info("Token renovado com sucesso.");
                return token;
            } else {
                log.error("Erro ao renovar token. Status: [{}], Body: [{}]", response.statusCode(), response.body());
                throw new TokenRefreshException("Não foi possível renovar o token na HubSpot.");
            }
        } catch (Exception e) {
            log.error("Erro ao tentar atualizar token: {}", e.getMessage(), e);
            throw new TokenRefreshException("Erro técnico ao atualizar token.");
        }
    }
}
