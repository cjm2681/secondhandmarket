package com.secondHand.SecondHandMarket.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    // Redis 키 구조: "RT:{userId}"
    // 접두사(RT:) 사용 이유:
    //   이메일 인증(EV:), 채팅(chat:) 등 다른 Redis 키와 충돌 방지
    //   Redis CLI에서 keys RT:* 로 Refresh Token만 조회 가능
    private static final String KEY_PREFIX = "RT:";  // "RT:{userId}"
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    // Refresh Token 저장
    // TTL을 JwtProperties의 refreshTokenExpiration과 동일하게 설정
    // 이유: 토큰 만료 시 Redis에서도 자동 삭제 → 불필요한 데이터 누적 방지
    //       TTL이 지나면 find()에서 null 반환 → 로그인 필요
    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    // Refresh Token 조회
    // Optional 반환: null 가능성을 타입으로 명시 → NullPointerException 방지
    // 사용처(AuthService.reissue)에서 orElseThrow로 처리
    public Optional<String> find(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + userId));
    }

    // Refresh Token 삭제 (로그아웃)
    // 삭제 후 해당 Refresh Token으로 재발급 시도하면
    // find()에서 empty → REFRESH_TOKEN_NOT_FOUND 예외 → 재발급 차단
    // JWT는 Stateless라 서버가 토큰을 무효화할 수 없지만
    // Redis에서 삭제하면 실질적으로 로그아웃 효과를 냄
    public void delete(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}