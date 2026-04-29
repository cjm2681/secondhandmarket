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

@Controller     // @RestController가 아닌 이유: WebSocket 메시지는 HTTP 응답이 없음 → @ResponseBody 불필요
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    // @MessageMapping: STOMP 메시지 라우팅 (HTTP의 @RequestMapping과 유사)
    // 클라이언트가 /pub/chat/{roomId} 로 전송 → 이 메서드로 라우팅
    // (WebSocketConfig에서 setApplicationDestinationPrefixes("/pub") 설정했으므로
    //  실제 경로는 /pub + /chat/{roomId} = /pub/chat/{roomId})
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable("roomId") Long roomId,      // URL 경로의 roomId 추출
            @Payload ChatMessageRequest request,            // 메시지 바디 (JSON → 객체 역직렬화)
            Principal principal) {                          // StompAuthInterceptor에서 설정한 인증 정보


        // principal.getName() = userId (JwtProvider.getUserId에서 추출한 값)
        // HTTP 컨트롤러의 @AuthenticationPrincipal Long userId 와 동일한 역할
        Long senderId = Long.parseLong(principal.getName());
        chatService.sendMessage(roomId, senderId, request.getMessage());
    }
}