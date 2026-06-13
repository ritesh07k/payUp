package com.payUp.build.ledger.controller;

import com.payUp.build.common.response.ApiResponse;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.ledger.dto.BalanceResponse;
import com.payUp.build.ledger.dto.LedgerEntryResponse;
import com.payUp.build.ledger.service.LedgerService;
import com.payUp.build.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;
    private final MerchantRepository merchantRepository;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        ledgerService.getBalance(merchantId),
                        "Balance fetched"));
    }

    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> getEntries(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        ledgerService.getEntries(merchantId),
                        "Ledger entries fetched"));
    }

    private UUID getMerchantId(UserDetails userDetails) {
        return merchantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"))
                .getId();
    }
}