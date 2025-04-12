package com.example.oauth.controller;

import com.example.oauth.model.HubspotWebhookEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.SignatureException;
import java.util.List;


@Tag(name = "Webhook", description = "Recebimento de eventos do HubSpot")
@RequestMapping("/webhook")
public interface WebhookApi {

    @Operation(
            summary = "Recebe evento do Webhook do HubSpot",
            description = "Endpoint responsável por receber e processar os eventos enviados pelo Webhook do HubSpot."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento processado com sucesso",
                    content = @Content(
                            mediaType = "text/plain"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content(
                            mediaType = "text/plain"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao processar o evento",
                    content = @Content(
                            mediaType = "text/plain"
                    )
            )
    })
    @PostMapping
    ResponseEntity<String> getWebhook(
            @Parameter(
                    description = "Evento enviado pelo HubSpot",
                    required = true,
                    schema = @Schema(implementation = HubspotWebhookEvent.class)
            ) HttpServletRequest request,
            @RequestBody List<HubspotWebhookEvent> event
    ) throws SignatureException;

}
