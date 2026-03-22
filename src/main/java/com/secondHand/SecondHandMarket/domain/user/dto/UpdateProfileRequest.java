package com.secondHand.SecondHandMarket.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다")
    private String nickname;      // null이면 변경 안 함
}