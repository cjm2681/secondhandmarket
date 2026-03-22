package com.secondHand.SecondHandMarket.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "인증코드를 입력해주세요")
    private String code;

    @NotBlank(message = "새 비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String newPassword;
}