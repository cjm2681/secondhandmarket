package com.secondHand.SecondHandMarket.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    // Controller 파라미터가 아닌 곳에서 현재 로그인 유저의 role이 필요할 때 사용
    // (Service에서 파라미터로 전달하기 애매한 경우)
    //
    // 사용 위치:
    //   - ProductController.delete() → 본인 or ADMIN 삭제 권한 확인
    //   - BoardController.delete()   → 본인 or ADMIN 삭제 권한 확인
    //   - CommentController.delete() → 본인 or ADMIN 삭제 권한 확인
    //
    // @AuthenticationPrincipal과 차이점:
    //   @AuthenticationPrincipal → principal (userId) 꺼냄
    //   SecurityUtil.getCurrentUserRole() → authorities에서 role 꺼냄
    //   둘 다 SecurityContextHolder에서 가져오지만 꺼내는 정보가 다름
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities()
                .stream()
                .findFirst()              // 권한이 하나라면 첫 번째 = 유일한 권한
                .map(GrantedAuthority::getAuthority)    // GrantedAuthority → "ROLE_ADMIN"
                .orElse("")           // 인증 정보 없으면 빈 문자열
                .replace("ROLE_", "");  // "ROLE_ADMIN" → "ADMIN"
                                                        // "ROLE_USER"  → "USER"
    }
}