package com.payUp.build.payment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payUp.build.common.response.ApiResponse;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.payment.dto.CreateOrderRequest;
import com.payUp.build.payment.dto.OrderResponse;
import com.payUp.build.payment.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantRepository merchantRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateOrderRequest request) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        orderService.createOrder(merchantId, request),
                        "Order created"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderId) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getOrder(orderId, merchantId),
                        "Order fetched"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getOrders(merchantId),
                        "Orders fetched"));
    }

    private UUID getMerchantId(UserDetails userDetails) {
        return merchantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"))
                .getId();
    }
}