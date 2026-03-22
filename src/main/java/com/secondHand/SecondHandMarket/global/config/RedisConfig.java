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

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // Pub/Sub 메시지를 JSON으로 직렬화
    @Bean
    public RedisTemplate<String, ChatMessageResponse> chatRedisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, ChatMessageResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new JacksonJsonRedisSerializer<>(ChatMessageResponse.class));
        return template;
    }

    // Redis 메시지 리스너 등록
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

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }


}