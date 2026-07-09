package com.payup.payment.service;

import com.payup.common.exception.ResourceNotFoundException;
import com.payup.payment.dto.CreateOrderRequest;
import com.payup.payment.dto.OrderResponse;
import com.payup.payment.entity.Order;
import com.payup.payment.entity.OrderStatus;
import com.payup.payment.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse createOrder(UUID merchantId, CreateOrderRequest request) {
        Order order = new Order();
        order.setMerchantId(merchantId);
        order.setAmountPaise(request.getAmountPaise());
        order.setCurrency(request.getCurrency());
        order.setReceipt(request.getReceipt());
        order.setStatus(OrderStatus.CREATED);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public OrderResponse getOrder(UUID merchantId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getMerchantId().equals(merchantId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getAmountPaise(),
                order.getCurrency(),
                order.getReceipt(),
                order.getStatus()
        );
    }
}
