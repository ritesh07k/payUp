package com.payUp.build.credential.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payUp.build.credential.entity.ApiCredential;
import com.payUp.build.merchant.entity.Merchant;

@Repository
public interface ApiCredentialRepository extends JpaRepository<ApiCredential, UUID> {

    Optional<ApiCredential> findByApiKeyAndIsActiveTrue(String apiKey);

    List<ApiCredential> findByMerchant(Merchant merchant);
}