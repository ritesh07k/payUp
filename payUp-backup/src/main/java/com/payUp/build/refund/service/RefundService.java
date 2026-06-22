package com.payUp.build.refund.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payUp.build.exception.AppException;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.payment.entity.Payment;
import com.payUp.build.payment.entity.PaymentStatus;
import com.payUp.build.payment.repository.PaymentRepository;
import com.payUp.build.refund.dto.CreateRefundRequest;
import com.payUp.build.refund.dto.RefundResponse;
import com.payUp.build.refund.entity.Refund;
import com.payUp.build.refund.entity.RefundStatus;
import com.payUp.build.refund.repository.RefundRepository;
import com.payUp.build.webhook.service.WebhookService;
import com.payUp.build.events.EventPublisher;
import com.payUp.build.events.RefundEvent;
import com.payUp.build.ledger.entity.EntryType;
import com.payUp.build.ledger.entity.ReferenceType;
import com.payUp.build.ledger.service.LedgerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final WebhookService webhookService;
    private final EventPublisher eventPublisher;
    private final LedgerService ledgerService;

    @Transactional
    public RefundResponse initiateRefund(UUID merchantId,
            CreateRefundRequest request) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Payment not found");
        }

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new AppException(
                    "Only captured payments can be refunded",
                    HttpStatus.CONFLICT);
        }

        Long alreadyRefunded = refundRepository.sumRefundedAmount(payment);
        Long availableAmount = payment.getAmount() - alreadyRefunded;

        if (request.getAmount() > availableAmount) {
            throw new AppException(
                    "Refund amount exceeds available amount. Available: "
                            + availableAmount + " paise",
                    HttpStatus.CONFLICT);
        }

        return refundRepository
                .findByPaymentAndIdempotencyKey(payment, request.getIdempotencyKey())
                .map(this::toResponse)
                .orElseGet(() -> processNewRefund(merchant, payment, request));
    }

    private RefundResponse processNewRefund(Merchant merchant, Payment payment,
            CreateRefundRequest request) {

        Refund refund = Refund.builder()
                .payment(payment)
                .merchant(merchant)
                .amount(request.getAmount())
                .currency(payment.getCurrency())
                .status(RefundStatus.PROCESSING)
                .idempotencyKey(request.getIdempotencyKey())
                .reason(request.getReason())
                .build();

        refund = refundRepository.save(refund);

        boolean bankApproved = simulateBankRefund();

        if (bankApproved) {
            refund.setStatus(RefundStatus.SUCCESS);
            refund.setBankReferenceId("REFUND_REF_" + UUID.randomUUID()
                    .toString().substring(0, 8).toUpperCase());

            refund = refundRepository.save(refund);

            ledgerService.recordEntry(merchant, EntryType.DEBIT, refund.getAmount(),
                refund.getCurrency(), ReferenceType.REFUND, refund.getId(),
                "Refund issued for payment " + payment.getId());

            eventPublisher.publishRefundEvent(new RefundEvent(
                refund.getId(), payment.getId(), merchant.getId(),
                payment.getAmount(), payment.getCurrency(),
                RefundStatus.SUCCESS, "refund.success",
                java.time.LocalDateTime.now()));
        } else {
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason("Bank declined the refund");

            refund = refundRepository.save(refund);

            eventPublisher.publishRefundEvent(new RefundEvent(
                refund.getId(), payment.getId(), merchant.getId(),
                payment.getAmount(), payment.getCurrency(),
                RefundStatus.FAILED, "refund.failed",
                java.time.LocalDateTime.now()));
        }

        return toResponse(refund);
    }

    public List<RefundResponse> getRefundsByPayment(UUID paymentId, UUID merchantId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Payment not found");
        }

        return refundRepository.findByPayment(payment)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private boolean simulateBankRefund() {
        return Math.random() > 0.1;
    }

    private RefundResponse toResponse(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getPayment().getId(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getReason(),
                refund.getBankReferenceId(),
                refund.getFailureReason(),
                refund.getCreatedAt()
        );
    }
}