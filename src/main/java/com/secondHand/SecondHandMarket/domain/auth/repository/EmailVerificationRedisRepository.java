package com.secondHand.SecondHandMarket.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRedisRepository {

    private static final String KEY_PREFIX = "EV:";   // "EV:{email}"
    private static final long EXPIRATION = 300L;       // 5분

    private static final String VERIFIED_PREFIX = "EV:DONE:";  // "EV:DONE:{email}"

    private static final String RESET_PREFIX = "PW:RESET:";  // "PW:RESET:{email}"

    private final RedisTemplate<String, String> redisTemplate;

    // 인증코드 저장
    public void save(String email, String code) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + email,
                code,
                EXPIRATION,
                TimeUnit.SECONDS
        );
    }

    // 인증코드 조회
    public Optional<String> find(String email) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(KEY_PREFIX + email)
        );
    }

    // 인증 완료 후 삭제
    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }



    // 인증 완료 표시 저장 (verifyCode 성공 후 호출)
    public void saveVerified(String email) {
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + email,
                "true",
                30L,            // 30분 안에 회원가입 완료해야 함
                TimeUnit.MINUTES
        );
    }

    // 인증 완료 여부 확인
    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(VERIFIED_PREFIX + email)
        );
    }

    // 회원가입 완료 후 삭제
    public void deleteVerified(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }


    public void saveResetCode(String email, String code) {
        redisTemplate.opsForValue().set(
                RESET_PREFIX + email,
                code,
                10L,           // 10분
                TimeUnit.MINUTES
        );
    }

    public Optional<String> findResetCode(String email) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(RESET_PREFIX + email)
        );
    }

    public void deleteResetCode(String email) {
        redisTemplate.delete(RESET_PREFIX + email);
    }


}