package com.payup.refund.repository;

import com.payup.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {
    Optional<Refund> findByIdempotencyKey(String idempotencyKey);
    List<Refund> findByPaymentId(UUID paymentId);
}
