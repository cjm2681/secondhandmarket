package com.secondHand.SecondHandMarket.global.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


// @ConfigurationProperties: application.yaml의 특정 prefix를 클래스에 바인딩
// prefix = "jwt" → yaml의 jwt.secret, jwt.access-token-expiration 등을 자동 매핑
//
// @Value를 쓰지 않는 이유:
//   @Value는 필드마다 개별 선언 → JWT 관련 설정이 흩어짐
//   @ConfigurationProperties는 관련 설정을 한 클래스에서 관리 → 응집도 높음
@Getter
@Component
@ConfigurationProperties(prefix = "jwt")  // application.yml의 jwt: 매핑
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration;     // 단위: ms (15분 = 900000ms)
    private long refreshTokenExpiration;    // 단위: ms (7일 = 604800000ms)

    // @ConfigurationProperties는 setter 방식으로 yaml 값 바인딩
    // Lombok @Setter 대신 직접 선언한 이유:
    //   ConfigurationProperties 바인딩에 필요한 setter만 노출
    //   불필요한 setter 외부 노출 방지
    public void setSecret(String secret) { this.secret = secret; }
    public void setAccessTokenExpiration(long v) { this.accessTokenExpiration = v; }
    public void setRefreshTokenExpiration(long v) { this.refreshTokenExpiration = v; }
}