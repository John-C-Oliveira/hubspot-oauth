package com.example.oauth.service.impl;

import com.example.oauth.model.HubspotWebhookEvent;
import com.example.oauth.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    @Override
    public void processEvents(HubspotWebhookEvent event) {
        log.info("Evento do HubSpot de Id: [{}], processado com exito", event.getObjectId());
    }
}
