package com.payup.auth.dto;

import com.payup.auth.entity.CredentialStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CredentialCacheDto {
    private UUID id;
    private String email;
    private String passwordHash;
    private CredentialStatus status;
}
