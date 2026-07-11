package com.payup.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class WebhookEndpointResponse {
    private UUID id;
    private String url;
    private boolean active;
    private String secret;
}
