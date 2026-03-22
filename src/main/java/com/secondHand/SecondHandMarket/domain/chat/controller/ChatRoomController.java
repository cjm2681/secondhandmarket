package com.secondHand.SecondHandMarket.domain.chat.controller;

import com.secondHand.SecondHandMarket.domain.chat.dto.ChatMessageResponse;
import com.secondHand.SecondHandMarket.domain.chat.dto.ChatRoomResponse;
import com.secondHand.SecondHandMarket.domain.chat.service.ChatService;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Chat", description = "채팅 API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    // 채팅방 조회 또는 생성 (상품 페이지에서 "문의하기" 버튼)
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getOrCreateRoom(
            @RequestParam("productId") Long productId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                chatService.getOrCreateRoom(productId, userId)));
    }

    // 내 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.getMyRooms(userId)));
    }

    // 채팅 내역 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                chatService.getMessages(roomId, userId)));
    }


    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal Long userId) {
        chatService.markAsRead(roomId, userId);
        return ResponseEntity.ok(ApiResponse.ok("읽음 처리 완료", null));
    }


}