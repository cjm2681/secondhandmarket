package com.secondHand.SecondHandMarket.domain.user.controller;

import com.secondHand.SecondHandMarket.domain.user.dto.SignupRequest;
import com.secondHand.SecondHandMarket.domain.user.dto.UpdatePasswordRequest;
import com.secondHand.SecondHandMarket.domain.user.dto.UpdateProfileRequest;
import com.secondHand.SecondHandMarket.domain.user.dto.UserResponse;
import com.secondHand.SecondHandMarket.domain.user.service.UserService;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name= "User", description = "회원 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody SignupRequest request) {

        UserResponse response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("회원가입 성공", response));
    }

    // 회원 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(userId)));
    }



    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(userId)));
    }

    // 닉네임 수정
    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("프로필 수정 완료",
                userService.updateProfile(userId, request)));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 변경 완료", null));
    }


}