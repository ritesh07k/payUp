package com.payUp.build.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterWebhookRequest {

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "https://.*", message = "Webhook URL must use HTTPS")
    private String url;
}