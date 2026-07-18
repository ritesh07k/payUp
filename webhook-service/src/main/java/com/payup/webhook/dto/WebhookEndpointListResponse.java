package com.payup.webhook.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;
@Getter
@AllArgsConstructor
public class WebhookEndpointListResponse {
    private UUID id;
    private String url;
    private boolean active;
}
