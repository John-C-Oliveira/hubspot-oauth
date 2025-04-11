package com.example.oauth.exceptions.handlers;

public class HubspotIntegrationException extends RuntimeException {
    public HubspotIntegrationException(String message) {
        super(message);
    }

    public HubspotIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
