package com.payUp.build.merchant.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payUp.build.merchant.entity.Merchant;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByEmail(String email);

    boolean existsByEmail(String email);
}