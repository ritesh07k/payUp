package com.payup.payment.controller;
import com.payup.common.response.ApiResponse;
import com.payup.payment.dto.CapturePaymentRequest;
import com.payup.payment.dto.PaymentResponse;
import com.payup.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @GetMapping
    public ApiResponse<List<PaymentResponse>> listPayments(Authentication authentication) {
        UUID merchantId = UUID.fromString(authentication.getName());
        return ApiResponse.success(paymentService.listPayments(merchantId));
    }
    @PostMapping("/capture")
    public ApiResponse<PaymentResponse> capture(
            Authentication authentication,
            @Valid @RequestBody CapturePaymentRequest request) {
        UUID merchantId = UUID.fromString(authentication.getName());
        PaymentResponse response = paymentService.capture(merchantId, request);
        return ApiResponse.success(response);
    }
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getPayment(
            Authentication authentication,
            @PathVariable UUID paymentId) {
        UUID merchantId = UUID.fromString(authentication.getName());
        PaymentResponse response = paymentService.getPayment(merchantId, paymentId);
        return ApiResponse.success(response);
    }
}
