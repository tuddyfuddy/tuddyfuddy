package com.heejuk.tuddyfuddy.authservice.service;

import static com.heejuk.tuddyfuddy.authservice.constant.JWT_SET.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReissueService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_PREFIX = "refresh:";

    public void saveRefreshToken(String userId, String refreshToken) {
        String key = REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(
            key,
            refreshToken,
            REFRESH_TOKEN_EXPIRATION,
            TimeUnit.MILLISECONDS
        );
    }

    public Optional<String> findRefreshToken(String userId) {
        String key = REFRESH_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    public void deleteRefreshToken(String userId) {
        String key = REFRESH_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
