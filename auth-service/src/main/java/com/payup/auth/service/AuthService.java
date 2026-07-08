package com.payup.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payup.auth.config.KafkaConfig;
import com.payup.auth.dto.AuthResponse;
import com.payup.auth.dto.LoginRequest;
import com.payup.auth.dto.SignupRequest;
import com.payup.auth.events.MerchantCreatedEvent;
import com.payup.auth.security.JwtService;
import com.payup.common.exception.AppException;
import com.payup.common.exception.DuplicateResourceException;
import com.payup.auth.entity.Credential;
import com.payup.auth.entity.CredentialStatus;
import com.payup.auth.repository.CredentialRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuthResponse signup(SignupRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        Credential credential = Credential.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(CredentialStatus.PENDING)
                .build();

        credentialRepository.save(credential);

        // publish event so merchant-service creates the profile asynchronously
        MerchantCreatedEvent event = new MerchantCreatedEvent(
                credential.getId(),
                request.getBusinessName(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(KafkaConfig.MERCHANT_CREATED_TOPIC, credential.getId().toString(), event);
        log.info("Published MerchantCreated event for merchantId: {}", credential.getId());

        String accessToken = jwtService.generateToken(credential);
        String refreshToken = refreshTokenService.generateAndStore(credential.getId().toString());
        return new AuthResponse(accessToken, refreshToken, credential.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        Credential credential = credentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (credential.getStatus() == CredentialStatus.SUSPENDED) {
            throw new AppException("Account suspended", HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtService.generateToken(credential);
        String refreshToken = refreshTokenService.generateAndStore(credential.getId().toString());
        return new AuthResponse(accessToken, refreshToken, credential.getEmail());
    }

    public AuthResponse refresh(String refreshToken) {
        String merchantId = refreshTokenService.validateAndGetMerchantId(refreshToken);
        if (merchantId == null) {
            throw new AppException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        Credential credential = credentialRepository.findById(java.util.UUID.fromString(merchantId))
                .orElseThrow(() -> new AppException("Credential not found", HttpStatus.UNAUTHORIZED));

        refreshTokenService.rotate(refreshToken, merchantId);
        String newRefreshToken = refreshTokenService.generateAndStore(merchantId);
        String newAccessToken = jwtService.generateToken(credential);

        return new AuthResponse(newAccessToken, newRefreshToken, credential.getEmail());
    }

    public void logout(String accessToken, String refreshToken) {
        long remaining = jwtService.getRemainingValidity(accessToken);
        tokenBlacklistService.blacklist(accessToken, remaining);
        refreshTokenService.invalidate(refreshToken);
    }
}
