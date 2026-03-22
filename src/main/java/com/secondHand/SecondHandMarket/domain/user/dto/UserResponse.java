package com.secondHand.SecondHandMarket.domain.user.dto;

import com.secondHand.SecondHandMarket.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private boolean emailVerified;
    private LocalDateTime createdAt;

    // Entity → DTO 변환 (정적 팩토리 메서드)
    public static UserResponse from(User user) {
        return UserResponse.builder()
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
