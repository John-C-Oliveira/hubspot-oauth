package com.example.oauth.controller;

import com.example.oauth.service.OAuthService;
import com.example.oauth.service.impl.OAuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController implements OAuthApi {
    private final OAuthService oAuthService;

    @Override
    public String generateAuthorizationUrl() {
        return oAuthService.getAuthorizationUrl();
    }

    @Override
    public ResponseEntity<String> oauthCallback(String code, String error) {
        if (error != null) {
            log.error("Foi recebido um erro do HubSpot. Erro: [{}]", error);
            return ResponseEntity.badRequest().body("Erro recebido do HubSpot: " + error);
        }

        if (code == null || code.isEmpty()) {
            log.warn("Código de autorização ausente na URL de retorno");
            return ResponseEntity.badRequest().body("Código de autorização não informado.");
        }

        try {
            String msg = oAuthService.exchangeCodeForToken(code);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            log.error("Erro ao realizar conversão do código por token. Motivo: [{}]", e.getMessage());
            return ResponseEntity.status(500).body("Erro ao realizar conversão do código por token: " + e.getMessage());
        }
    }


}
