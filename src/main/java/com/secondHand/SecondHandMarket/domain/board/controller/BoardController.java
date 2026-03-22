package com.secondHand.SecondHandMarket.domain.board.controller;

import com.secondHand.SecondHandMarket.domain.board.dto.BoardCreateRequest;
import com.secondHand.SecondHandMarket.domain.board.dto.BoardListResponse;
import com.secondHand.SecondHandMarket.domain.board.dto.BoardResponse;
import com.secondHand.SecondHandMarket.domain.board.dto.BoardUpdateRequest;
import com.secondHand.SecondHandMarket.domain.board.service.BoardService;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import com.secondHand.SecondHandMarket.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Board", description = "게시판 API")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    //게시글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BoardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("게시글 작성 성공", boardService.create(userId, request)));
    }

    
    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardListResponse>>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getList(keyword, page)));
    }

    // 게시글 상세 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> getDetail(
            @PathVariable("boardId") Long boardId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getDetail(boardId, request)));
    }

    
    // 게시글 수정
    @PutMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> update(
            @PathVariable("boardId") Long boardId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BoardUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("수정 성공",
                boardService.update(boardId, userId, request)));
    }

    
    // 게시글 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("boardId") Long boardId,
            @AuthenticationPrincipal Long userId) {
        boardService.delete(boardId, userId, SecurityUtil.getCurrentUserRole());
        return ResponseEntity.ok(ApiResponse.ok("삭제 성공", null));
    }


    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<BoardListResponse>>> getMyBoards(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getMyBoards(userId, page)));
    }


}