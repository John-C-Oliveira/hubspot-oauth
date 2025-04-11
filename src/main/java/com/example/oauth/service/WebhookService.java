package com.example.oauth.service;

import com.example.oauth.model.HubspotWebhookEvent;

public interface WebhookService {
    void processEvents(HubspotWebhookEvent event);
}
