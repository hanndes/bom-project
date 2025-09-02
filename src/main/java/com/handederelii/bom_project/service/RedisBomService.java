package com.handederelii.bom_project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handederelii.bom_project.exceptions.RedisWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisBomService {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "BOM:";

    public String save(String mpn, int quantity, String requestedBy, Duration ttl) {
        String requestId = UUID.randomUUID().toString();
        try {

            BomEnvelope env = new BomEnvelope(mpn, quantity, requestedBy, Instant.now().toString());
            String json = objectMapper.writeValueAsString(env);
            redis.opsForValue().set(PREFIX + requestId, json, ttl);
            return requestId;
        } catch (Exception e) {
            log.error("Redis yazma hatası. key={}, ttl={}, msg={}", PREFIX + requestId, ttl, e.getMessage(), e);
            throw new RedisWriteException("BOM kaydı Redis'e yazılamadı", PREFIX + requestId, e);
        }
    }

    public Optional<String> getJson(String requestId) {
        String json = redis.opsForValue().get(PREFIX + requestId);
        return Optional.ofNullable(json);
    }

    /** Redis’e yazdığımız basit zarf */
    public record BomEnvelope(String mpn, int quantity, String requestedBy, String createdAt) {}
}
