package com.example.oauth.controller;

import com.example.oauth.exceptions.handlers.HubspotIntegrationException;
import com.example.oauth.model.ContactRequestDTO;
import com.example.oauth.model.ContactResponseDTO;
import com.example.oauth.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ContactController implements ContactApi {

    private final ContactService contactService;


    @Override
    public ResponseEntity<String> createContact(ContactRequestDTO contactRequest) {
        try {
            log.info("Criando contato no HubSpot. para cliente de Email: [{}]", contactRequest.getEmail());
            return contactService.createHubspotContact(contactRequest);
        }catch (Exception e){
            log.error("Erro ao criar contato no HubSpot. Motivo: [{}]", e.getMessage());
            return ResponseEntity.status(500).body("Erro ao realizar criação de contato no HubSpot. Motivo: " + e.getMessage());
        }
    }
}
