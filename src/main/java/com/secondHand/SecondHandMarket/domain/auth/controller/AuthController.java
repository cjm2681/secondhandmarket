package com.secondHand.SecondHandMarket.domain.auth.controller;

import com.secondHand.SecondHandMarket.domain.auth.dto.*;
import com.secondHand.SecondHandMarket.domain.auth.service.AuthService;
import com.secondHand.SecondHandMarket.domain.auth.service.EmailService;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final EmailService emailService;

    private final UserRepository userRepository;


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("로그인 성공", authService.login(request)));
    }

    // Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.ok("토큰 재발급 성공", authService.reissue(refreshToken)));
    }


    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok("로그아웃 성공", null));
    }



    // 인증코드 발송
    @PostMapping("/email")
    public ResponseEntity<ApiResponse<Void>> sendVerificationEmail(
            @Valid @RequestBody EmailRequest request) {

        // 이미 가입된 이메일이면 거부
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        emailService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("인증코드가 발송되었습니다", null));
    }

    // 인증코드 검증
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody EmailVerifyRequest request) {

        emailService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.ok("이메일 인증이 완료되었습니다", null));
    }



    // 비밀번호 재설정 코드 발송
    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetEmail(
            @Valid @RequestBody EmailRequest request) {
        emailService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("재설정 코드가 발송되었습니다", null));
    }

    // 비밀번호 재설정
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다", null));
    }


}