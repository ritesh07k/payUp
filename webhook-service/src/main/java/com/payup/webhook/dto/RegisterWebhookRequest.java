package com.payup.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterWebhookRequest {

    @NotBlank
    private String url;
}
