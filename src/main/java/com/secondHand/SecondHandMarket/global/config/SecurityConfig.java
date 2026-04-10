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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))    //JWT 사용 시 세션 안 씀
            .headers(headers -> headers
                    .frameOptions(frame -> frame.disable())  // H2 콘솔 iframe 허용
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/api/users/signup").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()        // 로그인, 재발급 허용
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // 상품 조회 비로그인 허용
                    .requestMatchers("/api/admin/**").hasRole("ADMIN") // 어드민만
                    .requestMatchers(HttpMethod.GET, "/api/boards/**").permitAll()  // 게시판 조회 비로그인 허용
                    .requestMatchers("/ws-chat/**").permitAll()  // WebSocket 엔드포인트
                    .requestMatchers("/api/chat/**").authenticated()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 어드민만 접근
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**"
                    ).permitAll()
                            .requestMatchers("/api/products/*/test-view-data").permitAll()
                    .anyRequest().authenticated()                       // 나머지는 로그인 필요
                    //.anyRequest().permitAll()  // 개발 초기엔 전부 허용
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 등록
        return http.build();
    }


    // ✅ Security 레벨에서 CORS 설정 (WebMvcConfig와 동일하게)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }



    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

}