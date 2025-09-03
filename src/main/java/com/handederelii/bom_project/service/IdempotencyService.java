package com.handederelii.bom_project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.handederelii.bom_project.exceptions.IdempotencyDuplicateException;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {


    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /* ---------- Public API ---------- */
    /**
     * Aynı scope + body (+actor) için işi sadece 1 kez çalıştırır.
     * - Body deterministik JSON’a çevrilir, SHA-256 hash alınır.
     * - Redis SETNX (setIfAbsent) ile kilit alınır.
     * - İş biterse "DONE" olarak işaretlenir; hata olursa kilit silinir.
     */
    public <T> T runOnce(String scope,
                         Object requestBody,
                         Duration ttl,
                         Supplier<T> action,
                         @Nullable String actorId) {

        String bodyHash = sha256Base64(toStableJsonBytes(requestBody));
        String key = buildKey(scope, bodyHash, actorId);

        long ttlMs = Math.max(1, ttl == null ? 0 : ttl.toMillis()); // ← 0'ı engelle
        Boolean acquired = redisTemplate
                .opsForValue()
                .setIfAbsent(key, "PENDING", ttlMs, TimeUnit.MILLISECONDS);

        if (!Boolean.TRUE.equals(acquired)) {
            throw new IdempotencyDuplicateException("Duplicate request (idempotent): " + scope);
        }

        try {
            T result = action.get();
            redisTemplate.opsForValue().set(key, "DONE", ttlMs, TimeUnit.MILLISECONDS);
            return result;
        } catch (RuntimeException ex) {
            redisTemplate.delete(key);
            throw ex;
        }
    }

    /**
     * Eğer body hash’ini dışarıda hesapladıysan ya da Idempotency-Key header’ı kullanıyorsan,
     * doğrudan bu hash üzerinden çalıştırmak için:
     */
    public <T> T runOnceWithHash(String scope,
                                 String bodyHash,
                                 Duration ttl,
                                 Supplier<T> action,
                                 @Nullable String actorId) {
        String key = buildKey(scope, bodyHash, actorId);

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "PENDING", ttl.toSeconds(), TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new IdempotencyDuplicateException("Duplicate request (idempotent): " + scope);
        }

        try {
            T result = action.get();
            redisTemplate.opsForValue().set(key, "DONE", ttl.toSeconds(), TimeUnit.SECONDS);
            return result;
        } catch (RuntimeException ex) {
            redisTemplate.delete(key);
            throw ex;
        }
    }

    /* ---------- Basit geriye dönük metodlar (istersen kullan) ---------- */

    /** Eski kullanımın için: sadece var mı yok mu bakar (yarışa dayanıklı değildir). */
    public boolean alreadyProcessed(String hash) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(hash));
    }

    /** Eski kullanımın için: hash'i 1 saat saklar (yarışa dayanıklı değildir). */
    public void markProcessed(String hash) {
        redisTemplate.opsForValue().set(hash, "processed", 1, TimeUnit.HOURS);
    }

    /* ---------- Helpers ---------- */

    private byte[] toStableJsonBytes(Object body) {
        try {
            // Aynı body -> aynı JSON (map entry’lerini sıralar)
            ObjectMapper stable = objectMapper.copy()
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            return stable.writeValueAsBytes(body);
        } catch (Exception e) {
            throw new IllegalStateException("Request body JSON'a çevrilemedi", e);
        }
    }

    private String sha256Base64(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(data));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 hash üretilemedi", e);
        }
    }

    private String buildKey(String scope, String bodyHash, @Nullable String actorId) {
        // Örn: idem:POST:/api/v1/bom:user123:AbCdEf...
        return "idem:" + scope + ":" + (actorId == null ? "-" : actorId) + ":" + bodyHash;
    }
}