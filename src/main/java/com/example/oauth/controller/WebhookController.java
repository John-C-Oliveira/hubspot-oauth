package com.example.oauth.controller;

import com.example.oauth.model.HubspotWebhookEvent;
import com.example.oauth.service.WebhookService;
import com.example.oauth.service.impl.WebhookServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController implements WebhookApi {

    private final WebhookService webhookService;
    public ResponseEntity<String> handleWebhook(@Valid @RequestBody HubspotWebhookEvent event) {
        log.info("Recebendo evento do HubSpot. Id do evento: [{}]", event.getObjectId());
        webhookService.processEvents(event);
        return ResponseEntity.ok("Webhook processado com sucesso.");
    }

}
