package com.secondHand.SecondHandMarket.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRedisRepository {

    // Redis 키 구조 설계
    // 접두사로 데이터 종류를 구분 → 키 충돌 방지 + 용도 파악 쉬움
    private static final String KEY_PREFIX = "EV:";   // 인증코드: "EV:{email}"

    private static final long EXPIRATION = 300L;       // 인증코드 TTL: 5분(초 단위)
                                                        // 짧은 이유: 코드 노출 위험 시간 최소화

    private static final String VERIFIED_PREFIX = "EV:DONE:";   // 인증 완료: "EV:DONE:{email}"

    private static final String RESET_PREFIX = "PW:RESET:";   // 비밀번호 재설정: "PW:RESET:{email}"


    private final RedisTemplate<String, String> redisTemplate;

    // 인증코드 저장 (5분 TTL)
    // TTL 만료 시 Redis가 자동 삭제 → 별도 삭제 로직 불필요
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
    // TTL: 30분 → 이 시간 내에 회원가입을 완료해야 함
    // 만료 시 isVerified() = false → 회원가입 불가 → 재인증 필요
    public void saveVerified(String email) {
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + email,
                "true",
                30L,            // 30분 안에 회원가입 완료해야 함
                TimeUnit.MINUTES
        );
    }

    // 인증 완료 여부 확인
    // hasKey(): 키 존재 여부만 확인 → 값 조회보다 빠름
    // Boolean.TRUE.equals(): hasKey()가 null 반환 가능 → NullPointerException 방지
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