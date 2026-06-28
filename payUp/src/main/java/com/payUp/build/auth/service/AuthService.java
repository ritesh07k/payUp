package com.payUp.build.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payUp.build.auth.dto.AuthResponse;
import com.payUp.build.auth.dto.LoginRequest;
import com.payUp.build.auth.dto.SignupRequest;
import com.payUp.build.auth.security.JwtService;
import com.payUp.build.exception.AppException;
import com.payUp.build.exception.DuplicateResourceException;
import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.merchant.entity.MerchantStatus;
import com.payUp.build.merchant.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse signup(SignupRequest request) {
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        Merchant merchant = Merchant.builder()
                .businessName(request.getBusinessName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(MerchantStatus.PENDING)
                .build();

        merchantRepository.save(merchant);

        String accessToken = jwtService.generateToken(merchant);
        String refreshToken = refreshTokenService.generateAndStore(merchant.getId().toString());
        return new AuthResponse(accessToken, refreshToken, merchant.getEmail(), merchant.getBusinessName());
    }

    public AuthResponse login(LoginRequest request) {
        Merchant merchant = merchantRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), merchant.getPasswordHash())) {
            throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (merchant.getStatus() == MerchantStatus.SUSPENDED) {
            throw new AppException("Account suspended", HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtService.generateToken(merchant);
        String refreshToken = refreshTokenService.generateAndStore(merchant.getId().toString());
        return new AuthResponse(accessToken, refreshToken, merchant.getEmail(), merchant.getBusinessName());
    }

    public AuthResponse refresh(String refreshToken) {
        String merchantId = refreshTokenService.validateAndGetMerchantId(refreshToken);
        if (merchantId == null) {
            throw new AppException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        Merchant merchant = merchantRepository.findById(java.util.UUID.fromString(merchantId))
                .orElseThrow(() -> new AppException("Merchant not found", HttpStatus.UNAUTHORIZED));

        refreshTokenService.rotate(refreshToken, merchantId);
        String newRefreshToken = refreshTokenService.generateAndStore(merchantId);
        String newAccessToken = jwtService.generateToken(merchant);

        return new AuthResponse(newAccessToken, newRefreshToken, merchant.getEmail(), merchant.getBusinessName());
    }

    public void logout(String accessToken, String refreshToken) {
        // blacklist the access token for its remaining lifetime
        long remaining = jwtService.getRemainingValidity(accessToken);
        tokenBlacklistService.blacklist(accessToken, remaining);
        // invalidate the refresh token
        refreshTokenService.invalidate(refreshToken);
    }
}
