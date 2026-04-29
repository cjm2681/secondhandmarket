package com.secondHand.SecondHandMarket.global.config;

import com.secondHand.SecondHandMarket.global.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// @EnableWebSecurity: Spring Security 활성화
// JWT 기반 인증을 사용하므로 세션·폼 로그인 비활성화 필요
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF(Cross-Site Request Forgery) 비활성화
                // CSRF 공격은 쿠키 기반 세션 인증에서 발생
                // JWT는 쿠키가 아닌 Authorization 헤더로 전달 → CSRF 위협 없음
            .csrf(csrf -> csrf.disable())

                // 세션 미사용 설정 (STATELESS)
                // JWT는 서버가 인증 상태를 저장하지 않음 (Stateless)
                // 세션을 생성하지 않으면 Scale-out 시 세션 공유 문제 없음
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))    //JWT 사용 시 세션 안 씀

                // H2 콘솔은 iframe으로 렌더링됨
                // 기본 설정에서 X-Frame-Options: DENY → H2 콘솔 접근 불가
                // 개발 환경에서만 disable 적용 필요
            .headers(headers -> headers
                    .frameOptions(frame -> frame.disable())  // H2 콘솔 iframe 허용
            )

            .authorizeHttpRequests(auth -> auth
                        // OPTIONS 요청 허용: CORS preflight 요청 (브라우저가 실제 요청 전 허용 여부 확인)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/api/users/signup").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()        // 로그인, 재발급 허용
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // 상품 조회 비로그인 허용

                        // /api/admin/** 은 ROLE_ADMIN 만 접근 가능
                        // Spring Security에서 hasRole("ADMIN") = "ROLE_ADMIN" 권한 체크
                        // JwtFilter에서 "ROLE_" + role 형태로 부여했으므로 매칭됨
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    .requestMatchers(HttpMethod.GET, "/api/boards/**").permitAll()  // 게시판 조회 비로그인 허용

                        // WebSocket 최초 연결 엔드포인트는 permitAll
                        // 실제 인증은 StompAuthInterceptor에서 CONNECT 시점에 처리
                    .requestMatchers("/ws-chat/**").permitAll()  // WebSocket 엔드포인트
                    .requestMatchers("/api/chat/**").authenticated()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 어드민만 접근
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**"
                    ).permitAll()
                    .anyRequest().authenticated()                       // 나머지는 로그인 필요
                    //.anyRequest().permitAll()  // 개발 초기엔 전부 허용
            )
                // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 등록
                // 이유: Spring Security 기본 필터(UsernamePasswordAuthenticationFilter)가
                //       실행되기 전에 JWT를 먼저 파싱하고 SecurityContext에 인증 정보를 설정해야 함
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 등록
        return http.build();
    }

    // CORS 설정 (Cross-Origin Resource Sharing)
    // 프론트(localhost:5173)와 백엔드(localhost:8080)가 다른 포트 → CORS 필요
    // Security 레벨에서 설정하는 이유:
    //   CORS 요청이 Security 필터를 통과하기 전에 거부될 수 있으므로
    //   WebMvcConfigurer가 아닌 Security에서 함께 설정해야 정상 동작
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // ExposedHeaders: 브라우저가 읽을 수 있도록 허용할 응답 헤더
        // Authorization 헤더는 기본적으로 브라우저에서 읽기 불가 → 명시적 허용 필요
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);    // 쿠키/인증 정보 포함 허용
        config.setMaxAge(3600L);         // preflight 캐시 시간 (초): 3600초 = 1시간 동안 재요청 없음

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    // BCryptPasswordEncoder: 비밀번호 단방향 해시 암호화
    // 특징: 같은 값이어도 매번 다른 해시 생성 (salt 자동 포함)
    // 검증은 matches(rawPassword, encodedPassword)로만 가능
    // Spring Bean으로 등록하는 이유: UserService, AuthService 등 여러 곳에서 주입받아 사용
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

}