package com.payup.payment.service;
import com.payup.common.exception.ResourceNotFoundException;
import com.payup.payment.dto.CapturePaymentRequest;
import com.payup.payment.dto.PaymentResponse;
import com.payup.payment.entity.*;
import com.payup.payment.events.PaymentCapturedEvent;
import com.payup.payment.repository.LedgerEntryRepository;
import com.payup.payment.repository.OrderRepository;
import com.payup.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Transactional
    public PaymentResponse capture(UUID merchantId, CapturePaymentRequest request) {
        // Idempotency check first — if this key was already processed, return existing result
        var existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getMerchantId().equals(merchantId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setMerchantId(merchantId);
        payment.setAmountPaise(order.getAmountPaise());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIdempotencyKey(request.getIdempotencyKey());
        Payment savedPayment = paymentRepository.save(payment);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setMerchantId(merchantId);
        ledgerEntry.setReferenceId(savedPayment.getId());
        ledgerEntry.setReferenceType("PAYMENT");
        ledgerEntry.setEntryType(LedgerEntryType.CREDIT);
        ledgerEntry.setAmountPaise(savedPayment.getAmountPaise());
        ledgerEntryRepository.save(ledgerEntry);
        PaymentCapturedEvent event = new PaymentCapturedEvent(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                merchantId,
                savedPayment.getAmountPaise(),
                savedPayment.getCurrency(),
                Instant.now()
        );
        kafkaTemplate.send("payment-events", savedPayment.getId().toString(), event);
        return toResponse(savedPayment);
    }
    public PaymentResponse getPayment(UUID merchantId, UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new ResourceNotFoundException("Payment not found");
        }
        return toResponse(payment);
    }
    public List<PaymentResponse> listPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmountPaise(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus()
        );
    }
}
