package com.secondHand.SecondHandMarket.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondHand.SecondHandMarket.domain.chat.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPublisher {

    private final RedisTemplate<String, ChatMessageResponse> chatRedisTemplate;
    private final ObjectMapper objectMapper;

    // Redis Pub/Sub 발행 담당
    // 채널 네이밍: "chat:{roomId}" → 채팅방별 채널 분리
    // 분리 이유: 채팅방 A의 메시지가 채팅방 B 구독자에게 전달되는 문제 방지
    public void publish(ChatMessageResponse message) {
        String channel = "chat:" + message.getRoomId();
        try {
            String json = objectMapper.writeValueAsString(message);

            // convertAndSend: 객체를 JSON으로 직렬화 후 Redis 채널에 발행
            // 이 시점에 해당 채널을 구독 중인 ChatSubscriber.onMessage()가 호출됨
            chatRedisTemplate.convertAndSend(channel, message);
            log.info("Redis 발행 완료 - channel: {}, message: {}", channel, json);
        } catch (JsonProcessingException e) {
            log.error("Redis 발행 실패: {}", e.getMessage());
        }
    }
}