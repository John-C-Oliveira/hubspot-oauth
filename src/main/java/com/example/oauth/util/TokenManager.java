package com.example.oauth.util;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.TokenNotFoundException;
import com.example.oauth.model.OAuthToken;
import com.example.oauth.model.TokenResponse;
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
        if (oAuthToken == null || isExpired(oAuthToken)) {
            log.info("Token em cache ausente ou expirado. Tentando buscar novo token...");
            oAuthToken = fetchLatestToken()
                    .map(token -> refreshIfExpired(token))
                    .orElseThrow(() -> new TokenNotFoundException("Nenhum token válido encontrado"));
        }
        return oAuthToken;
    }

    private Optional<OAuthToken> fetchLatestToken() {
        return tokenRepository.findTopByOrderByCreatedAtDesc();
    }

    private OAuthToken refreshIfExpired(OAuthToken token) {
        if (isExpired(token)) {
            log.info("Token expirado, iniciando tentativa de atualização");
            return refreshToken(token);
        }
        return token;
    }

    private boolean isExpired(OAuthToken token) {
        return token.getCreatedAt().plusSeconds(token.getExpiresIn()).isBefore(LocalDateTime.now());
    }

    private OAuthToken refreshToken(OAuthToken token) {
        try {
            String body = "grant_type=refresh_token"
                    + CLIENT_ID + config.getClientId()
                    + CLIENT_SECRET + config.getClientSecret()
                    + REFRESH_TOKEN + token.getRefreshToken();

            HttpResponse<String> response = HttpUtil.postForm(config.getTokenUrl(), body);

            if (HttpStatusCode.valueOf(response.statusCode()).is2xxSuccessful()) {
                TokenResponse refreshed = objectMapper.readValue(response.body(), TokenResponse.class);
                token.setAccessToken(refreshed.getAccessToken());
                token.setExpiresIn(refreshed.getExpiresIn());
                token.setCreatedAt(LocalDateTime.now());
                tokenRepository.save(token);
                log.info("Token atualizado com sucesso no HubSpot.");
                return token;
            } else {
                throw new TokenNotFoundException("Não foi possível renovar o token na HubSpot. HttpStatus: "+response.statusCode()+" Body: " + response.body());
            }

        } catch (Exception e) {
            throw new TokenNotFoundException("Erro ao atualizar token no HubSpot. Motivo: " + e.getMessage());
        }
    }
}
