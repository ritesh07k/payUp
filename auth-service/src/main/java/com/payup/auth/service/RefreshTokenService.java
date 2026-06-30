package com.payup.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${application.security.jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String PREFIX = "refresh:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateAndStore(String merchantId) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        redisTemplate.opsForValue().set(
            PREFIX + token,
            merchantId,
            refreshExpiration,
            TimeUnit.MILLISECONDS
        );
        return token;
    }

    public String validateAndGetMerchantId(String token) {
        return redisTemplate.opsForValue().get(PREFIX + token);
    }

    public void rotate(String oldToken, String merchantId) {
        redisTemplate.delete(PREFIX + oldToken);
    }

    public void invalidate(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
