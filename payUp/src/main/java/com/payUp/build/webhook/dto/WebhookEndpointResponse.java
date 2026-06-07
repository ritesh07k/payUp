package com.payUp.build.webhook.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WebhookEndpointResponse {

    private UUID id;
    private String url;
    private boolean isActive;
}