package com.example.oauth.controller;

import com.example.oauth.model.ContactRequestDTO;
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
        log.info("Criando contato no HubSpot para cliente com email: [{}]", contactRequest.getEmail());
        return contactService.createHubspotContact(contactRequest);
    }


}
