package com.secondHand.SecondHandMarket.domain.board.controller;

import com.secondHand.SecondHandMarket.domain.board.dto.CommentCreateRequest;
import com.secondHand.SecondHandMarket.domain.board.dto.CommentResponse;
import com.secondHand.SecondHandMarket.domain.board.service.CommentService;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import com.secondHand.SecondHandMarket.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성 (parentId 없으면 일반 댓글, 있으면 대댓글)
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable("boardId") Long boardId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("댓글 작성 성공",
                        commentService.create(boardId, userId, request)));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok("댓글 수정 성공",
                commentService.update(commentId, userId, body.get("content"))));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal Long userId) {
        commentService.delete(commentId, userId, SecurityUtil.getCurrentUserRole());
        return ResponseEntity.ok(ApiResponse.ok("댓글 삭제 성공", null));
    }
}