package com.example.oauth.service;

import com.example.oauth.model.HubspotWebhookEvent;
import jakarta.servlet.http.HttpServletRequest;

import java.security.SignatureException;
import java.util.List;

public interface WebhookService {
    void processEvents(HttpServletRequest request,List<HubspotWebhookEvent> event) throws SignatureException;
}
