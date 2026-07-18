package com.payup.refund.controller;
import com.payup.common.response.ApiResponse;
import com.payup.refund.dto.CreateRefundRequest;
import com.payup.refund.dto.RefundResponse;
import com.payup.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;
    @GetMapping
    public ApiResponse<List<RefundResponse>> listRefunds(Authentication authentication) {
        UUID merchantId = UUID.fromString(authentication.getName());
        return ApiResponse.success(refundService.listRefunds(merchantId));
    }
    @PostMapping
    public ApiResponse<RefundResponse> createRefund(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateRefundRequest request) {
        UUID merchantId = UUID.fromString(authentication.getName());
        String token = authHeader.substring(7);
        RefundResponse response = refundService.createRefund(merchantId, token, request);
        return ApiResponse.success(response);
    }
}
