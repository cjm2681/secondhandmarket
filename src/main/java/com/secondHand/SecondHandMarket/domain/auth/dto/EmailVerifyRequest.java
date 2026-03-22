package com.secondHand.SecondHandMarket.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerifyRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "인증코드를 입력해주세요")
    @Size(min = 6, max = 6, message = "인증코드는 6자리입니다")
    private String code;
}