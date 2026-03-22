package com.secondHand.SecondHandMarket.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondHand.SecondHandMarket.domain.chat.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Redis에서 받은 메시지 → STOMP 구독자에게 전달
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            ChatMessageResponse chatMessage =
                    objectMapper.readValue(json, ChatMessageResponse.class);

            // /sub/chat/{roomId} 구독자에게 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/sub/chat/" + chatMessage.getRoomId(),
                    chatMessage
            );
            log.info("STOMP 전달 완료 - roomId: {}", chatMessage.getRoomId());
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", e.getMessage());
        }
    }
}