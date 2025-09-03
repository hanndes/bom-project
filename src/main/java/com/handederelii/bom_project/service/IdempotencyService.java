package com.handederelii.bom_project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.handederelii.bom_project.entity.Bom;
import com.handederelii.bom_project.entity.BomStatus;
import com.handederelii.bom_project.exceptions.IdempotencyDuplicateException;
import com.handederelii.bom_project.repositories.BomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.zip.CRC32;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedisTemplate<String, String> redis;
    private final ObjectMapper mapper;
    private final BomRepository bomRepo;

    /* ========= OVERLOAD: actor String (username/principal) + tip ========= */
    public <T> T runOnce(String scope,
                         Object body,
                         Duration ttl,
                         Supplier<T> work,
                         String actorKey,
                         Class<T> resultType) {
        long actorId = toStableActorId(actorKey);
        return runOnce(scope, actorId, body, ttl, work, resultType);
    }

    /* ========= ÇEKİRDEK: actor long + tip ========= */
    @Transactional
    public <T> T runOnce(String scope,
                         long actorId,
                         Object body,
                         Duration ttl,
                         Supplier<T> work,
                         Class<T> resultType) {

        String canonical = toCanonicalJson(body);
        String bodyHash  = sha256Hex(canonical);
        String lockKey   = "idem:" + scope + ":" + actorId + ":" + bodyHash;

        // 1) Redis lock (SETNX)
        boolean locked = Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(lockKey, "PENDING", ttl)
        );
        if (!locked) {
            return getPreviousResultOrThrow(scope, actorId, bodyHash, resultType);
        }

        // 2) DB — PENDING (üçlü UNIQUE)
        Bom row = Bom.builder()
                .scope(scope)
                .actorId(actorId)
                .bodyHash(bodyHash)
                .status(BomStatus.PENDING)
                .build();

        try {
            bomRepo.save(row);
        } catch (DataIntegrityViolationException dup) {
            return getPreviousResultOrThrow(scope, actorId, bodyHash, resultType);
        }

        try {
            // 3) Asıl iş
            T result = work.get();

            // 4) DONE + result_json
            row.setStatus(BomStatus.DONE);
            row.setResultJson(writeJson(result));
            bomRepo.save(row);

            // 5) Redis'te DONE (kısa süre tutmak istersen)
            redis.opsForValue().set(lockKey, "DONE", ttl);
            return result;

        } catch (RuntimeException ex) {
            // Tekrar deneyebilmek için kilidi kaldır
            redis.delete(lockKey);
            throw ex;
        }
    }

    /* ----------------- helpers ----------------- */

    @Transactional(readOnly = true)
    public <T> T getPreviousResultOrThrow(String scope,
                                           long actorId,
                                           String bodyHash,
                                           Class<T> resultType) {
        Optional<Bom> prev = bomRepo.findByScopeAndActorIdAndBodyHash(scope, actorId, bodyHash);
        if (prev.isPresent() && prev.get().getResultJson() != null) {
            return readJson(prev.get().getResultJson(), resultType);
        }
        throw new IdempotencyDuplicateException("Already processing the same request.");
    }

    private String toCanonicalJson(Object body) {
        try {
            ObjectMapper stable = mapper.copy()
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            return stable.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Body canonical JSON'a çevrilemedi", e);
        }
    }

    private String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 üretilemedi", e);
        }
    }

    private <T> T readJson(String json, Class<T> type) {
        try { return mapper.readValue(json, type); }
        catch (Exception e) { throw new IllegalStateException("result_json parse edilemedi", e); }
    }

    private String writeJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { throw new IllegalStateException("result_json yazılamadı", e); }
    }

    /** username/principal String → stabil long id */
    private long toStableActorId(String actorKey) {
        if (actorKey == null || actorKey.isBlank()) return 0L;
        try {
            return Long.parseLong(actorKey); // numeric ise direkt
        } catch (NumberFormatException ignore) {
            CRC32 crc = new CRC32();
            crc.update(actorKey.getBytes(StandardCharsets.UTF_8));
            return crc.getValue(); // aynı username için aynı değer
        }
    }
}
