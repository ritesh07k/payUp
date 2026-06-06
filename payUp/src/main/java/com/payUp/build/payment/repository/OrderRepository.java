package com.payUp.build.payment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.payment.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByMerchant(Merchant merchant);
}