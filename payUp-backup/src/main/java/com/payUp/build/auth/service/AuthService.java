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

        String token = jwtService.generateToken(merchant);
        return new AuthResponse(token, merchant.getEmail(), merchant.getBusinessName());
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

        String token = jwtService.generateToken(merchant);
        return new AuthResponse(token, merchant.getEmail(), merchant.getBusinessName());
    }
}