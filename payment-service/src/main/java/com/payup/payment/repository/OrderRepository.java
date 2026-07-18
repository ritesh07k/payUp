package com.payup.payment.repository;

import com.payup.payment.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}
