package com.secondHand.SecondHandMarket.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.secondHand.SecondHandMarket.domain.chat.dto.ChatMessageResponse;
import com.secondHand.SecondHandMarket.domain.chat.service.ChatSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 일반 String 데이터용 RedisTemplate (조회수, 토큰, 이메일 인증 등)
    // key/value 모두 StringRedisSerializer → 사람이 읽을 수 있는 형태로 저장
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // 채팅 메시지 전용 RedisTemplate
    // value를 ChatMessageResponse 객체로 직렬화 (JSON)
    // 일반 redisTemplate과 분리한 이유:
    //   ChatMessageResponse는 복잡한 객체 → JSON 직렬화 필요
    //   String 타입과 혼용하면 역직렬화 오류 발생 가능
    @Bean
    public RedisTemplate<String, ChatMessageResponse> chatRedisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, ChatMessageResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new JacksonJsonRedisSerializer<>(ChatMessageResponse.class));
        return template;
    }

    // Redis Pub/Sub 메시지 리스너 컨테이너
    // 역할: Redis에서 발행된 메시지를 ChatSubscriber에게 전달
    //
    // 흐름:
    //   ChatPublisher.publish() → Redis "chat:{roomId}" 채널에 발행
    //   RedisMessageListenerContainer가 감지 → ChatSubscriber.onMessage() 호출
    //   ChatSubscriber → STOMP 브로커에 전달 → 구독 중인 클라이언트에게 전송
    //
    // PatternTopic("chat:*"): 모든 채팅방 채널 구독 (채팅방 ID 무관)
    // 채팅방별 채널 분리("chat:1", "chat:2" 등) → 다른 채팅방 메시지 간섭 없음
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            ChatSubscriber chatSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // 채팅 채널 패턴 구독: chat:* (모든 채팅방)
        container.addMessageListener(chatSubscriber,
                new PatternTopic("chat:*"));
        return container;
    }

    // JavaTimeModule 등록: LocalDateTime 직렬화/역직렬화 지원
    // 기본 ObjectMapper는 LocalDateTime을 배열([2026,4,29,...])로 변환
    // JavaTimeModule + WRITE_DATES_AS_TIMESTAMPS 비활성화 → "2026-04-29T12:00:00" 형태로 저장
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }


}