package com.example.oauth.service;

import com.example.oauth.model.ContactRequestDTO;
import com.example.oauth.model.ContactResponseDTO;
import org.springframework.http.ResponseEntity;

public interface ContactService {
    ResponseEntity<String> createHubspotContact(ContactRequestDTO contactData);
}
