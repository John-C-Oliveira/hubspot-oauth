package com.example.oauth.service.impl;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.ContactAlreadyExistsException;
import com.example.oauth.exceptions.handlers.HubspotIntegrationException;
import com.example.oauth.exceptions.handlers.InvalidPayloadException;
import com.example.oauth.exceptions.handlers.SignatureInvalidException;
import com.example.oauth.model.ContactRequestDTO;
import com.example.oauth.model.ContactResponseDTO;
import com.example.oauth.repository.OAuthToken;
import com.example.oauth.service.ContactService;
import com.example.oauth.service.OAuthService;
import com.example.oauth.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactServiceImpl implements ContactService {

    private final OAuthService oAuthService;
    private final HubSpotOAuthConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResponseEntity<String> createHubspotContact(ContactRequestDTO dto) {
        OAuthToken token = oAuthService.getValidTokenSync();
        String jsonPayload = convertDtoToJson(dto);
        HttpResponse<String> response = sendContactToHubspot(jsonPayload, token);
        validateContactResponse(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response.body());
    }

    private String convertDtoToJson(ContactRequestDTO dto) {
        try {
            Map<String, Object> requestBody = Map.of("properties", dto);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter DTO para JSON. Motivo: [{}]", e.getMessage());
            throw new InvalidPayloadException("Erro ao preparar dados do contato para envio à HubSpot.");
        }
    }

    private HttpResponse<String> sendContactToHubspot(String json, OAuthToken token) {
        try {
            return HttpUtil.postJson(config.getCreationContact(), json, token.getAccessToken());
        } catch (Exception e) {
            log.error("Erro ao enviar contato para HubSpot. Motivo: [{}]", e.getMessage());
            throw new HubspotIntegrationException("Erro ao se comunicar com a HubSpot.");
        }
    }

    private void validateContactResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();

        if (statusCode == 409) {
            log.warn("Contato já existe. Body: [{}]", response.body());
            throw new ContactAlreadyExistsException("O contato já existe na HubSpot.");
        }

        if (statusCode == 401) {
            log.warn("Token inválido ou expirado. Body: [{}]", response.body());
            throw new SignatureInvalidException("Token inválido ou expirado.");
        }

        if (HttpStatusCode.valueOf(statusCode).is2xxSuccessful()) {
            try {
                ContactResponseDTO contactResponseDTO = objectMapper.readValue(response.body(), ContactResponseDTO.class);
                log.info("Contato criado com ID: [{}]", contactResponseDTO.getId());
            } catch (JsonProcessingException e) {
                log.error("Erro ao interpretar resposta da HubSpot. Body: [{}]", response.body());
                throw new HubspotIntegrationException("Resposta da HubSpot inválida.");
            }
        } else {
            log.error("Falha ao criar contato. Status: [{}], Body: [{}]", statusCode, response.body());
            throw new HubspotIntegrationException("HubSpot retornou erro ao criar o contato.");
        }
    }

}
