package com.secondHand.SecondHandMarket.global.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "jwt")  // application.yml의 jwt: 매핑
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;

    // ConfigurationProperties는 setter 방식으로 바인딩
    public void setSecret(String secret) { this.secret = secret; }
    public void setAccessTokenExpiration(long v) { this.accessTokenExpiration = v; }
    public void setRefreshTokenExpiration(long v) { this.refreshTokenExpiration = v; }
}