package com.payUp.build.credential.dto;

import com.payUp.build.credential.entity.KeyType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCredentialRequest {

    private KeyType keyType;
}