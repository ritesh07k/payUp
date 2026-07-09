package com.payup.refund.service;

import com.payup.common.exception.AppException;
import com.payup.refund.client.PaymentClient;
import com.payup.refund.dto.CreateRefundRequest;
import com.payup.refund.dto.RefundResponse;
import com.payup.refund.entity.Refund;
import com.payup.refund.entity.RefundStatus;
import com.payup.refund.events.RefundProcessedEvent;
import com.payup.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public RefundResponse createRefund(UUID merchantId, String bearerToken, CreateRefundRequest request) {

        var existing = refundRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        PaymentClient.PaymentDetails payment = paymentClient.fetchPayment(request.getPaymentId(), bearerToken);

        if (!"CAPTURED".equals(payment.status())) {
            throw new AppException("Only captured payments can be refunded", HttpStatus.BAD_REQUEST);
        }

        if (request.getAmountPaise() > payment.amountPaise()) {
            throw new AppException("Refund amount exceeds payment amount", HttpStatus.BAD_REQUEST);
        }

        Refund refund = new Refund();
        refund.setPaymentId(payment.id());
        refund.setMerchantId(merchantId);
        refund.setAmountPaise(request.getAmountPaise());
        refund.setCurrency(payment.currency());
        refund.setReason(request.getReason());
        refund.setStatus(RefundStatus.PROCESSED);
        refund.setIdempotencyKey(request.getIdempotencyKey());

        Refund saved = refundRepository.save(refund);

        RefundProcessedEvent event = new RefundProcessedEvent(
                saved.getId(),
                saved.getPaymentId(),
                merchantId,
                saved.getAmountPaise(),
                saved.getCurrency(),
                Instant.now()
        );
        kafkaTemplate.send("refund-events", saved.getId().toString(), event);

        return toResponse(saved);
    }

    private RefundResponse toResponse(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getPaymentId(),
                refund.getAmountPaise(),
                refund.getCurrency(),
                refund.getStatus()
        );
    }
}
