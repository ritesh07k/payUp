package com.payUp.build.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payUp.build.payment.entity.Order;
import com.payUp.build.payment.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderAndIdempotencyKey(Order order, String idempotencyKey);

    List<Payment> findByOrder(Order order);
}
