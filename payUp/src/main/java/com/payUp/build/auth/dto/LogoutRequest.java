package com.payUp.build.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
