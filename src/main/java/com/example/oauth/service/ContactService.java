package com.example.oauth.service;

import com.example.oauth.model.ContactRequestDTO;
import org.springframework.http.ResponseEntity;

public interface ContactService {
    ResponseEntity<String> createHubspotContact(ContactRequestDTO contactData);
}
