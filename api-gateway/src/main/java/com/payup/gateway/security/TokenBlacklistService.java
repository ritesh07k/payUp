package com.payup.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    public Mono<Boolean> isBlacklisted(String token) {
        return reactiveStringRedisTemplate.hasKey("blacklist:" + token);
    }
}
