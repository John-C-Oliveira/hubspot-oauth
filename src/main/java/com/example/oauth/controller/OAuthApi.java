package com.example.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "OAuth", description = "Fluxo de autenticação OAuth com o HubSpot")
@RequestMapping("/oauth")
public interface OAuthApi {

    @Operation(
            summary = "Gera a URL de autorização do HubSpot",
            description = "Retorna a URL que o usuário deve acessar para autorizar o aplicativo no HubSpot."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "URL de autorização gerada com sucesso",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @GetMapping("/authorize-url")
    String generateAuthorizationUrl();

    @Operation(
            summary = "Callback de autorização OAuth",
            description = "Endpoint chamado pelo HubSpot após o usuário autorizar o aplicativo. Realiza a troca do código de autorização por um token de acesso."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token de acesso obtido com sucesso",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação ou código ausente",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao trocar o código por token",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @GetMapping("/callback")
    ResponseEntity<String> oauthCallback(
            @Parameter(description = "Código de autorização recebido do HubSpot")
            @RequestParam(value = "code", required = false) String code,

            @Parameter(description = "Mensagem de erro retornada pelo HubSpot")
            @RequestParam(value = "error", required = false) String error
    );
}
