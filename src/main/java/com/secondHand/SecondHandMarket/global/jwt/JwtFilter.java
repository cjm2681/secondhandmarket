package com.secondHand.SecondHandMarket.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


// OncePerRequestFilter: HTTP 요청당 정확히 한 번만 실행 보장
// 일반 Filter는 서블릿 포워딩(forward) 시 중복 실행될 수 있음
// Spring Security 필터 체인에 등록되어 모든 요청을 가로챔
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {  // 요청당 한 번만 실행

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰 있고 유효할 때만 인증 처리
        // 토큰 없는 요청 → SecurityContext에 인증 정보 미설정 → 익명 사용자로 처리
        // 이후 SecurityConfig의 authorizeHttpRequests에서 접근 허용/차단 판단
        if (token != null) {
            try {
                if (jwtProvider.validateToken(token)) {
                    Long userId = jwtProvider.getUserId(token);
                    String role = jwtProvider.getRole(token);

                    // Spring Security가 인식하는 Authentication 객체 생성
                    // UsernamePasswordAuthenticationToken: Spring Security 인증 객체
                    // principal = userId → 컨트롤러에서 @AuthenticationPrincipal Long userId 로 꺼냄
                    // credentials = null → 이미 JWT로 인증했으므로 비밀번호 불필요
                    // authorities = ROLE_USER 또는 ROLE_ADMIN → @PreAuthorize, hasRole() 등에서 사용
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,         // principal: 주인 식별자
                                    null,            // credentials: 비밀번호 (JWT는 불필요)
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))     // authorities: 권한 목록
                            );

                    // SecurityContext에 저장 → 이후 컨트롤러까지 인증 정보 전달
                    // SecurityContextHolder: ThreadLocal 기반으로 현재 요청의 인증 정보 보관
                    // 요청 처리가 끝나면 자동으로 초기화 (SecurityContextPersistenceFilter가 처리)
                    // 같은 요청 내 어디서든 SecurityContextHolder.getContext().getAuthentication()으로 접근 가능
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // 만료/유효하지 않은 토큰 → 인증 정보 설정 안 함
                // → Spring Security가 401로 처리
                SecurityContextHolder.clearContext();
            }
        }
        // 다음 필터로 요청 전달 (필터 체인 계속 진행)
        filterChain.doFilter(request, response);
    }

    // Header에서 "Bearer {token}" 파싱
    // Authorization 헤더에서 토큰 추출
    // 형식: "Bearer {token}"
    // "Bearer " (7글자) 이후의 토큰 문자열만 반환
    // StringUtils.hasText(): null + 빈 문자열 + 공백 문자열 모두 걸러냄
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}