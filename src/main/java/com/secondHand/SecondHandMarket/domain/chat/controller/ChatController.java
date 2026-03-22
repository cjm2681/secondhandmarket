package com.secondHand.SecondHandMarket.domain.chat.controller;

import com.secondHand.SecondHandMarket.domain.chat.dto.ChatMessageRequest;
import com.secondHand.SecondHandMarket.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    // 클라이언트가 /pub/chat/{roomId} 로 메시지 전송
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable("roomId") Long roomId,
            @Payload ChatMessageRequest request,
            Principal principal) {

        Long senderId = Long.parseLong(principal.getName());
        chatService.sendMessage(roomId, senderId, request.getMessage());
    }
}