package com.secondHand.SecondHandMarket.global.jwt;

import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    // SecretKey를 필드로 분리한 이유:
    //   Keys.hmacShaKeyFor()는 매번 호출하면 비용 발생
    //   @PostConstruct로 애플리케이션 시작 시 1회만 생성 후 재사용
    private SecretKey secretKey;

    // @PostConstruct: Spring Bean 초기화 완료 후 한 번만 실행
    // 생성자 대신 사용하는 이유:
    //   @RequiredArgsConstructor 생성자 실행 시점에는 jwtProperties 주입이 안 됐을 수 있음
    //   @PostConstruct는 주입 완료 후 실행 → 안전하게 값 사용 가능
    @PostConstruct
    public void init() {
        // HMAC-SHA256 알고리즘용 SecretKey 생성
        // JWT 서명에 사용 → 서버만 알고 있는 키로 서명 → 위변조 감지 가능
        // 256bit(32바이트) 이상의 시크릿 키 필요 (HS256 스펙)
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // Access Token 생성
    // subject: 토큰 주인 식별자 (userId를 String으로 저장)
    // claim("role"): 권한 정보 (USER / ADMIN) → Spring Security 인가에 활용
    // signWith(secretKey): HMAC-SHA256으로 서명 → 서버에서 서명 검증 가능
    public String createAccessToken(Long userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(secretKey)
                .compact();     // 최종 JWT 문자열 생성 (Header.Payload.Signature)
    }

    // Refresh Token 생성
    // Access Token과 달리 role 클레임 없음
    // 이유: Refresh Token은 재발급 용도로만 사용 → 직접 API 접근 불가
    //        role이 없으면 JwtFilter에서 인증 객체를 만들 수 없음
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // 토큰에서 role 추출
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 토큰 유효성 검증
    // 만료 예외(ExpiredJwtException)를 다른 예외와 분리하는 이유:
    //   만료 → 클라이언트가 Refresh Token으로 재발급 시도 가능 → EXPIRED_TOKEN 반환
    //   위변조/형식오류 → 재발급 불가 → INVALID_TOKEN 반환
    //   클라이언트는 에러코드로 재발급 필요 여부를 판단
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰 파싱 내부 메서드
    // verifyWith(secretKey): 서명 검증 → 위변조된 토큰 거부
    // parseSignedClaims: 서명 검증 + 파싱을 한 번에 처리
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();  // Header와 Signature를 제외한 실제 데이터 부분
    }
}