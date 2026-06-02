package com.payUp.build.credential.dto;

import java.util.UUID;

import com.payUp.build.credential.entity.KeyType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CredentialResponse {

    private UUID id;
    private String apiKey;
    private String apiSecret;
    private KeyType keyType;
    private boolean isActive;
    private String note;
}