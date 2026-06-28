package com.payUp.build.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payUp.build.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
        "/api/v1/auth/login",
        "/api/v1/auth/signup"
    );
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SECONDS = 60;
    private static final long BLOCK_SECONDS = 3600;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!RATE_LIMITED_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String blockedKey = "rate_limit:blocked:" + ip;
        String counterKey = "rate_limit:login:" + ip;

        // check if already blocked
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockedKey))) {
            writeError(response, "Too many attempts. Try again in 1 hour.");
            return;
        }

        // increment counter
        Long count = redisTemplate.opsForValue().increment(counterKey);

        // set TTL on first request only
        if (count != null && count == 1) {
            redisTemplate.expire(counterKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        // block if limit exceeded
        if (count != null && count > MAX_REQUESTS) {
            redisTemplate.opsForValue().set(blockedKey, "true", BLOCK_SECONDS, TimeUnit.SECONDS);
            redisTemplate.delete(counterKey);
            writeError(response, "Too many attempts. Try again in 1 hour.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
            response.getWriter(),
            ApiResponse.error(message)
        );
    }
}
