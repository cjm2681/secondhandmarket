package com.secondHand.SecondHandMarket.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondHand.SecondHandMarket.domain.chat.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


// MessageListener: Redis Pub/Sub 메시지 수신 인터페이스
// RedisMessageListenerContainer에 등록되어 "chat:*" 패턴 채널 메시지 감지
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
            // Redis에서 받은 raw bytes → JSON 문자열 → ChatMessageResponse 객체
            String json = new String(message.getBody());
            ChatMessageResponse chatMessage =
                    objectMapper.readValue(json, ChatMessageResponse.class);


            // SimpMessagingTemplate: STOMP 브로커를 통해 특정 목적지 구독자에게 메시지 전송
            // /sub/chat/{roomId} 를 구독 중인 클라이언트에게 브로드캐스트
            // Redis Pub/Sub → STOMP 브로드캐스트 연결 지점
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