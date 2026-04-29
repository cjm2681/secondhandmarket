package com.secondHand.SecondHandMarket.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // Swagger UI에서 JWT 인증 설정
        // bearerAuth 이름으로 Bearer Token 스키마 정의
        // → Swagger 화면 우측 상단 "Authorize" 버튼에서 토큰 입력 가능
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")        // Authorization: Bearer {token} 형태
                .bearerFormat("JWT")     // UI에 JWT 힌트 표시 (실제 검증은 서버가 담당)
                .name("Authorization");

        // 전역 보안 요구사항: 모든 API에 기본으로 bearerAuth 적용
        // 개별 API에서 @SecurityRequirement로 오버라이드 가능
        SecurityRequirement securityRequirement =
                new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("SecondHand Market API")
                        .description("중고거래 플랫폼 API 명세서")
                        .version("v1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}