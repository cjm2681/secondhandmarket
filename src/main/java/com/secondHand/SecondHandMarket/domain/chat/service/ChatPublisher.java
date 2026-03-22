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

    // Redis 채널에 메시지 발행
    public void publish(ChatMessageResponse message) {
        String channel = "chat:" + message.getRoomId();
        try {
            String json = objectMapper.writeValueAsString(message);
            chatRedisTemplate.convertAndSend(channel, message);
            log.info("Redis 발행 완료 - channel: {}, message: {}", channel, json);
        } catch (JsonProcessingException e) {
            log.error("Redis 발행 실패: {}", e.getMessage());
        }
    }
}