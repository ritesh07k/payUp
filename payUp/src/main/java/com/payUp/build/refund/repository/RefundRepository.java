package com.payUp.build.refund.repository;

import com.payUp.build.payment.entity.Payment;
import com.payUp.build.refund.entity.Refund;
import com.payUp.build.refund.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    Optional<Refund> findByPaymentAndIdempotencyKey(Payment payment,
            String idempotencyKey);

    List<Refund> findByPayment(Payment payment);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
           "WHERE r.payment = :payment " +
           "AND r.status IN ('SUCCESS', 'PENDING', 'PROCESSING')")
    Long sumRefundedAmount(@Param("payment") Payment payment);
}