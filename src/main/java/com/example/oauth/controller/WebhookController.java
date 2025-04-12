package com.example.oauth.controller;

import com.example.oauth.model.HubspotWebhookEvent;
import com.example.oauth.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController implements WebhookApi {

    private final WebhookService webhookService;


    @Override
    public ResponseEntity<String> getWebhook(HttpServletRequest request, @RequestBody List<HubspotWebhookEvent> events) throws SignatureException {

        log.info("Recebendo eventos do HubSpot. Quantidade de registros: [{}]", events.size());
        webhookService.processEvents(request, events);
        return ResponseEntity.status(204).body("Eventos processados com sucesso");
    }


}
