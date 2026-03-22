package com.secondHand.SecondHandMarket.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    // 현재 로그인한 유저의 role 반환
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("")
                .replace("ROLE_", "");  // "ROLE_ADMIN" → "ADMIN"
    }
}