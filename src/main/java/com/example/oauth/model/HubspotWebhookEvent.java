package com.example.oauth.model;

import lombok.Data;

@Data
public class HubspotWebhookEvent {
    private String subscriptionType;
    private String eventType;
    private Long objectId;
    private Long occurredAt;


}
