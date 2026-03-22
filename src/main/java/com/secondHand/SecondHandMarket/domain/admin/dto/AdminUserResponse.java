package com.secondHand.SecondHandMarket.domain.admin.dto;

import com.secondHand.SecondHandMarket.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String role;
    private String status;      // ACTIVE / BANNED
    private boolean emailVerified;
    private LocalDateTime createdAt;

    // 프론트에서 이 값 보고 버튼 텍스트 결정
    // status == ACTIVE → "정지" 버튼 표시
    // status == BANNED → "정지 해제" 버튼 표시
    public boolean isBanned() {
        return "BANNED".equals(this.status);
    }

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}