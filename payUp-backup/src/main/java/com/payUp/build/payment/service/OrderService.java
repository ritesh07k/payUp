package com.payUp.build.payment.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.payment.dto.CreateOrderRequest;
import com.payUp.build.payment.dto.OrderResponse;
import com.payUp.build.payment.entity.Order;
import com.payUp.build.payment.entity.OrderStatus;
import com.payUp.build.payment.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;

    public OrderResponse createOrder(UUID merchantId, CreateOrderRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        Order order = Order.builder()
                .merchant(merchant)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .receipt(request.getReceipt())
                .status(OrderStatus.CREATED)
                .build();

        return toResponse(orderRepository.save(order));
    }

    public OrderResponse getOrder(UUID orderId, UUID merchantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        return toResponse(order);
    }

    public List<OrderResponse> getOrders(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        return orderRepository.findByMerchant(merchant)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getAmount(),
                order.getCurrency(),
                order.getReceipt(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}