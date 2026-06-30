package com.payup.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.payup.auth.dto.*;
import com.payup.auth.dto.LoginRequest;
import com.payup.auth.dto.LogoutRequest;
import com.payup.auth.dto.SignupRequest;
import com.payup.auth.service.AuthService;
import com.payup.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<com.payup.auth.dto.AuthResponse>> signup(
            @RequestBody @Valid SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Merchant registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<com.payup.auth.dto.AuthResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity
                .ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody @Valid RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity
                .ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody @Valid LogoutRequest request) {
        authService.logout(request.getAccessToken(), request.getRefreshToken());
        return ResponseEntity
                .ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
