package com.secondHand.SecondHandMarket.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "RT:";  // "RT:{userId}"
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    // 저장
    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    // 조회
    public Optional<String> find(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + userId));
    }

    // 삭제 (로그아웃)
    public void delete(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}