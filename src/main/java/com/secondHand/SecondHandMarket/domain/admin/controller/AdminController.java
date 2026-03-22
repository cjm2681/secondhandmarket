package com.secondHand.SecondHandMarket.domain.admin.controller;

import com.secondHand.SecondHandMarket.domain.admin.dto.AdminUserResponse;
import com.secondHand.SecondHandMarket.domain.admin.service.AdminService;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 전체 회원 목록 조회
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUsers(keyword, page)));
    }

    // 특정 회원 정보 조회 (닉네임 클릭 시)
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUser(userId)));
    }

    // 회원 정지 / 정지 해제 토글
    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<AdminUserResponse>> toggleBan(
            @PathVariable("userId") Long userId) {
        AdminUserResponse response = adminService.toggleBan(userId);
        String message = response.isBanned() ? "회원 정지 완료" : "정지 해제 완료";
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    // 판매글 삭제
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable("productId") Long productId) {
        adminService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.ok("판매글 삭제 완료", null));
    }

    // 게시글 삭제
    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable("boardId") Long boardId) {
        adminService.deleteBoard(boardId);
        return ResponseEntity.ok(ApiResponse.ok("게시글 삭제 완료", null));
    }

    // 댓글/대댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable("commentId") Long commentId) {
        adminService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.ok("댓글 삭제 완료", null));
    }
}