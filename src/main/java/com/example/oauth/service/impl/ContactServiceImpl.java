package com.example.oauth.service.impl;

import com.example.oauth.config.HubSpotOAuthConfig;
import com.example.oauth.exceptions.handlers.HubspotIntegrationException;
import com.example.oauth.exceptions.handlers.InvalidPayloadException;
import com.example.oauth.model.ContactRequestDTO;
import com.example.oauth.model.ContactResponseDTO;
import com.example.oauth.model.OAuthToken;
import com.example.oauth.service.ContactService;
import com.example.oauth.service.OAuthService;
import com.example.oauth.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final OAuthService oAuthService;
    private final HubSpotOAuthConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public ResponseEntity<String> createHubspotContact(ContactRequestDTO dto) {
        OAuthToken token = oAuthService.getValidTokenSync();
        String jsonPayload = convertDtoToJson(dto);
        HttpResponse<String> response = sendContactToHubspot(jsonPayload, token);
        validateHubspotResponse(response);
        return ResponseEntity.status(201).body(response.body());
    }

    private String convertDtoToJson(ContactRequestDTO dto) {
        try {
            Map<String, Object> requestBody = Map.of("properties", dto);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter DTO para JSON. Motivo: [{}]", e.getMessage());
            throw new InvalidPayloadException("Falha ao serializar contato");
        }
    }

    private HttpResponse<String> sendContactToHubspot(String json, OAuthToken token) {
        try {
            return HttpUtil.postJson(config.getCreationContact(), json, token.getAccessToken());
        } catch (Exception e) {
            log.error("Erro ao enviar contato para HubSpot. Motivo: [{}]", e.getMessage());
            throw new HubspotIntegrationException("Falha ao chamar HubSpot");
        }
    }

    private void validateHubspotResponse(HttpResponse<String> response) {
        try {
            if (HttpStatusCode.valueOf(response.statusCode()).is2xxSuccessful()){
                log.info("Sucesso ao criar contato no HubSpot.");
                ContactResponseDTO contactResponseDTO= objectMapper.readValue(response.body(), ContactResponseDTO.class);
                log.info("Contato de ID: [{}], criado no HubSpot",contactResponseDTO.getId());
            }else {
                log.error("Erro ao criar contato no HubSpot. Status: [{}], Body: [{}]", response.statusCode(), response.body());
                throw new HubspotIntegrationException("Falha ao criar contato no HubSpot");
            }

        } catch (JsonProcessingException e) {
            log.error("Erro ao realizar o parser na resposta do HubSpot. Motivo: [{}]", e.getMessage());
            throw new HubspotIntegrationException("Resposta inválida da HubSpot");
        }
    }



}
