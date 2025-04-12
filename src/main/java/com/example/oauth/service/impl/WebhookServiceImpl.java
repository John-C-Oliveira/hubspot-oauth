package com.example.oauth.service.impl;

import com.example.oauth.model.HubspotWebhookEvent;
import com.example.oauth.service.WebhookService;
import com.example.oauth.util.HubspotWebhookVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {
    private final HubspotWebhookVerifier verifier;

    @Override
    public void processEvents(HttpServletRequest request, List<HubspotWebhookEvent> events) throws SignatureException {
        log.info("Processando eventos do HubSpot");
        verifier.isValidRequest(request, events);
        for (HubspotWebhookEvent event : events) {
            log.info("Evento do HubSpot de Id: [{}], processado com exito", event.getObjectId());
        }

    }
}
