package com.heejuk.tuddyfuddy.notificationservice.service;

import com.heejuk.tuddyfuddy.notificationservice.dto.request.FcmTokenRequest;
import com.heejuk.tuddyfuddy.notificationservice.entity.FcmToken;
import com.heejuk.tuddyfuddy.notificationservice.repository.FcmTokenRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository tokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String FCM_TOKEN_PREFIX = "fcm:token:";
    private static final Duration TOKEN_TTL = Duration.ofDays(7);

    @Transactional
    public void saveToken(FcmTokenRequest request) {

        String userId = request.userId();
        String fcmToken = request.fcmToken();

        // 1. DB 저장 (Write-Through)
        FcmToken token = tokenRepository.findByUserId(UUID.fromString(userId))
            .map(t -> {
                t.updateToken(fcmToken);
                return t;
            })
            .orElseGet(() -> FcmToken.builder()
                .fcmToken(fcmToken)
                .userId(UUID.fromString(userId))
                .build());

        tokenRepository.save(token);

        // 2. Redis 캐시 업데이트
        try {
            redisTemplate.opsForValue().set(
                getRedisKey(userId),
                fcmToken,
                TOKEN_TTL
            );
        } catch (Exception e) {
            log.error("Failed to cache FCM token in Redis for userId: {}", userId, e);
        }
    }

    public Optional<String> getFcmToken(String userId) {

        // 1. Redis에서 조회 (Cache-Aside)
        try {
            String cachedToken = redisTemplate.opsForValue().get(getRedisKey(userId));
            if (cachedToken != null) {
                return Optional.of(cachedToken);
            }
        } catch (Exception e) {
            log.error("Failed to get FCM token from Redis for userId: {}", userId, e);
        }

        // 2. Cache Miss: DB 조회 후 Redis 캐시 갱신
        return tokenRepository.findByUserId(UUID.fromString(userId))
            .map(token -> {
                try {
                    redisTemplate.opsForValue().set(
                        getRedisKey(userId),
                        token.getFcmToken(),
                        TOKEN_TTL
                    );
                } catch (Exception e) {
                    log.error("Failed to cache FCM token after DB lookup for userId: {}", userId,
                        e);
                }
                return token.getFcmToken();
            });
    }

    private String getRedisKey(String userId) {
        return FCM_TOKEN_PREFIX + userId;
    }

    public void checkRedisCache(String userId) {
        String key = getRedisKey(userId);

        // 값 확인
        String cachedToken = redisTemplate.opsForValue().get(key);
        log.info("Redis 캐시 확인 - userId: {}, cached token: {}", userId, cachedToken);

        // TTL 확인
        Long ttl = redisTemplate.getExpire(key);
        log.info("Redis TTL 확인 - userId: {}, TTL: {}초", userId, ttl);

        // 키 존재 여부 확인
        Boolean hasKey = redisTemplate.hasKey(key);
        log.info("Redis 키 존재 여부 - userId: {}, exists: {}", userId, hasKey);
    }

    // 모든 FCM 토큰 캐시 확인
    public void checkAllCachedTokens() {
        Set<String> keys = redisTemplate.keys(FCM_TOKEN_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String userId = key.replace(FCM_TOKEN_PREFIX, "");
                String token = redisTemplate.opsForValue().get(key);
                log.info("캐시된 토큰 - userId: {}, token: {}", userId, token);
            }
        }
    }

    // 캐시 수동 갱신
    public void refreshCache(String userId) {
        tokenRepository.findByUserId(UUID.fromString(userId))
            .ifPresent(token -> {
                redisTemplate.opsForValue().set(
                    getRedisKey(userId),
                    token.getFcmToken(),
                    TOKEN_TTL
                );
                log.info("캐시 수동 갱신 완료 - userId: {}", userId);
            });
    }
}
