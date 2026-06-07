package com.payUp.build.payment.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payUp.build.exception.AppException;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.payment.dto.CreatePaymentRequest;
import com.payUp.build.payment.dto.PaymentResponse;
import com.payUp.build.payment.entity.Order;
import com.payUp.build.payment.entity.OrderStatus;
import com.payUp.build.payment.entity.Payment;
import com.payUp.build.payment.entity.PaymentStatus;
import com.payUp.build.payment.repository.OrderRepository;
import com.payUp.build.payment.repository.PaymentRepository;
import com.payUp.build.webhook.service.WebhookService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final WebhookService webhookService;

    @Transactional
    public PaymentResponse initiatePayment(UUID merchantId,
            CreatePaymentRequest request) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            throw new AppException("Order already paid", HttpStatus.CONFLICT);
        }

        // idempotency check
        return paymentRepository
                .findByOrderAndIdempotencyKey(order, request.getIdempotencyKey())
                .map(this::toResponse)
                .orElseGet(() -> processNewPayment(merchant, order, request));
    }

    private PaymentResponse processNewPayment(Merchant merchant, Order order,
            CreatePaymentRequest request) {

        Payment payment = Payment.builder()
                .order(order)
                .merchant(merchant)
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .status(PaymentStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .paymentMethod(request.getPaymentMethod())
                .build();

        payment = paymentRepository.save(payment);

        // simulate bank processing
        boolean bankApproved = simulateBankResponse();

        if (bankApproved) {
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setBankReferenceId("BANK_REF_" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase());
             order.setStatus(OrderStatus.PAID);
            webhookService.dispatchEvent(merchant, "payment.captured", toResponse(payment));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Bank declined the transaction");
            order.setStatus(OrderStatus.FAILED);
            webhookService.dispatchEvent(merchant, "payment.failed", toResponse(payment));
        }

        orderRepository.save(order);
        return toResponse(paymentRepository.save(payment));
    }

    private boolean simulateBankResponse() {
        // 80% success rate for testing
        return Math.random() > 0.2;
    }

    public PaymentResponse getPayment(UUID paymentId, UUID merchantId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Payment not found");
        }

        return toResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByOrder(UUID orderId, UUID merchantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        return paymentRepository.findByOrder(order)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getBankReferenceId(),
                payment.getFailureReason(),
                payment.getCreatedAt()
        );
    }
}