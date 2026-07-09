package com.payup.payment.controller;

import com.payup.common.response.ApiResponse;
import com.payup.payment.dto.CreateOrderRequest;
import com.payup.payment.dto.OrderResponse;
import com.payup.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        UUID merchantId = UUID.fromString(authentication.getName());
        OrderResponse response = orderService.createOrder(merchantId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            Authentication authentication,
            @PathVariable UUID orderId) {
        UUID merchantId = UUID.fromString(authentication.getName());
        OrderResponse response = orderService.getOrder(merchantId, orderId);
        return ApiResponse.success(response);
    }
}
